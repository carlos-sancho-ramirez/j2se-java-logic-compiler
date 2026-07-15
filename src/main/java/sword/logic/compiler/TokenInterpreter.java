package sword.logic.compiler;

import sword.collections.*;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.expressions.AdditionExpression;
import sword.logic.syntax_tree.expressions.AndExpression;
import sword.logic.syntax_tree.expressions.ArrayConcatenationExpression;
import sword.logic.syntax_tree.expressions.ArrayConstructor;
import sword.logic.syntax_tree.expressions.ArrayValueAtExpression;
import sword.logic.syntax_tree.expressions.BooleanLiteralExpression;
import sword.logic.syntax_tree.expressions.ComplexExpression;
import sword.logic.syntax_tree.expressions.DifferentFromExpression;
import sword.logic.syntax_tree.expressions.DivisionExpression;
import sword.logic.syntax_tree.expressions.EqualThanExpression;
import sword.logic.syntax_tree.expressions.Expression;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.expressions.FunctionExecutionExpression;
import sword.logic.syntax_tree.expressions.FunctionExpression;
import sword.logic.syntax_tree.expressions.FunctionParameter;
import sword.logic.syntax_tree.expressions.GreaterOrEqualThanExpression;
import sword.logic.syntax_tree.expressions.GreaterThanExpression;
import sword.logic.syntax_tree.expressions.IfExpression;
import sword.logic.syntax_tree.expressions.IntegerLiteralExpression;
import sword.logic.syntax_tree.expressions.LowerOrEqualThanExpression;
import sword.logic.syntax_tree.expressions.LowerThanExpression;
import sword.logic.syntax_tree.expressions.ModuleExpression;
import sword.logic.syntax_tree.expressions.MultiplicationExpression;
import sword.logic.syntax_tree.expressions.OrExpression;
import sword.logic.syntax_tree.expressions.ReferenceExpression;
import sword.logic.syntax_tree.expressions.RegisterConstructor;
import sword.logic.syntax_tree.expressions.RegisterFieldAccessExpression;
import sword.logic.syntax_tree.expressions.StringLiteralExpression;
import sword.logic.syntax_tree.expressions.SubtractionExpression;
import sword.logic.syntax_tree.statements.ConstantDefinitionStatement;
import sword.logic.syntax_tree.statements.Statement;
import sword.logic.syntax_tree.statements.TypeAliasStatement;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.RegisterType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import java.io.IOException;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class TokenInterpreter {
    private final TokenParser mParser;

    public TokenInterpreter(TokenParser parser) {
        ensureNonNull(parser);
        mParser = parser;
    }

    private void throwSemanticError(String message, Token token) throws SemanticErrorException {
        throw new SemanticErrorException(message, token.getLine(), token.getColumn());
    }

    private Token nextTokenOrThrow(String message) throws IOException, SyntaxErrorException, UnexpectedEndOfFileException {
        final Token token = mParser.next();
        if (token == null) {
            throw new UnexpectedEndOfFileException(message);
        }
        else {
            return token;
        }
    }

    private void validateNextToken(String expectedText, String failureMessage) throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        final Token token = nextTokenOrThrow(failureMessage);
        if (!token.getText().equals(expectedText)) {
            throwSemanticError(failureMessage, token);
        }
    }

    private Type interpretType(ImmutableMap<String, Type> knownTypes) throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        final Token typeToken = nextTokenOrThrow("Expected type");
        final String typeTokenText = typeToken.getText();
        if (knownTypes.containsKey(typeTokenText)) {
            return knownTypes.get(typeTokenText);
        }
        else if (typeTokenText.equals("Int")) {
            validateNextToken("[", "Expected '[' after 'Int'");
            final Token minToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("..", "Expected '..'");
            final Token maxToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("]", "Expected ']'");
            return new IntegerType(minToken, maxToken);
        }
        else if (typeTokenText.equals("Array")) {
            validateNextToken("[", "Expected '[' after 'Array'");
            final Type itemType = interpretType(knownTypes);
            validateNextToken("]", "Expected ']'");
            return new ArrayType(itemType);
        }
        else if (typeTokenText.equals("{")) {
            final ImmutableMap.Builder<Token, Type> map = new ImmutableHashMap.Builder<>();
            Token nameToken = nextTokenOrThrow("Expected property name for register");
            if (nameToken.getText().equals("}")) {
                throwSemanticError("Empty register definition", nameToken);
            }
            else {
                while (!nameToken.getText().equals("}")) {
                    validateNextToken(":", "Expected ':' after name");
                    map.put(nameToken, interpretType(knownTypes));
                    validateNextToken(";", "Expected ';' after type definition");
                    nameToken = nextTokenOrThrow("Expected property name for register, or '}'");
                }

                return new RegisterType(map.build());
            }
        }
        else {
            throwSemanticError("Unknown type '" + typeTokenText + "'", typeToken);
        }

        throw new IllegalStateException("This code should be unreachable");
    }

    private static final class ExpressionInterpretationResult {
        final Expression result;
        final Token closingToken;

        ExpressionInterpretationResult(Expression result, Token closingToken) {
            ensureNonNull(result, closingToken);
            this.result = result;
            this.closingToken = closingToken;
        }
    }

    private ImmutableMap<String, ConstantDefinitionStatement> mergeKnownConstants(
            ImmutableMap<String, ConstantDefinitionStatement> outerKnownConstants,
            List<Statement> scopeStatements) {
        final MutableMap<String, ConstantDefinitionStatement> result = outerKnownConstants.mutate();
        for (Statement statement : scopeStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result.put(constDef.getName().getText(), constDef);
            }
        }

        return result.toImmutable();
    }

    private ExpressionInterpretationResult interpretExpression(
            ImmutableMap<String, Type> knownTypes,
            ImmutableMap<String, ConstantDefinitionStatement> outerKnownConstants
    ) throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        final Expression[] expression = new Expression[6];

        /*
         * Operator precedence is:
         * ( ) [ ] { }               Not possible as value of this field
         * .
         * * / %
         * + -
         * == != >= <= > >
         * &
         * |
         * if then else         Not possible as value of this field
         * ->                   Not possible as value of this field
         * =                    Not possible as value of this field
         * , ;                  Not possible as value of this field
         */
        final Token[] operator = new Token[6];
        int accumulated = 0;
        Token assigningName = null;
        final MutableList<Statement> assignments = MutableList.empty();

        do {
            final Token token = nextTokenOrThrow("Expected expression");
            final String tokenText = token.getText();
            if (tokenText.equals(",") || tokenText.equals(":") || tokenText.equals(";") || tokenText.equals(")") || tokenText.equals("]") || tokenText.equals("}") || tokenText.equals("then") || tokenText.equals("else")) {
                if (accumulated == 0 && !assignments.isEmpty()) {
                    return new ExpressionInterpretationResult(new ComplexExpression(assignments.toImmutable(), null), token);
                }
                else if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression", token);
                }
                else {
                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*")) {
                            expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("/")) {
                            expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("+")) {
                            final Expression leftExpression = expression[accumulated / 2 - 1];
                            final Expression rightExpression = expression[accumulated / 2];
                            final boolean leftIsArray = leftExpression.resultingType() instanceof ArrayType;
                            final boolean leftIsInteger = leftExpression.resultingType() instanceof IntegerType;
                            final boolean rightIsArray = rightExpression.resultingType() instanceof ArrayType;
                            final boolean rightIsInteger = rightExpression.resultingType() instanceof IntegerType;

                            if (leftIsArray && !rightIsInteger) {
                                try {
                                    expression[accumulated / 2 - 1] = new ArrayConcatenationExpression(leftExpression, rightExpression.resultTo(leftExpression.resultingType()));
                                }
                                catch (TypeMismatchException e) {
                                    throwSemanticError(e.getMessage(), token);
                                }
                            }
                            else if (rightIsArray && !leftIsInteger) {
                                try {
                                    expression[accumulated / 2 - 1] = new ArrayConcatenationExpression(leftExpression.resultTo(rightExpression.resultingType()), rightExpression);
                                }
                                catch (TypeMismatchException e) {
                                    throwSemanticError(e.getMessage(), token);
                                }
                            }
                            else if (leftIsInteger && !rightIsArray) {
                                try {
                                    expression[accumulated / 2 - 1] = new AdditionExpression(leftExpression, rightExpression.resultTo(leftExpression.resultingType()));
                                }
                                catch (TypeMismatchException e) {
                                    throwSemanticError(e.getMessage(), token);
                                }
                            }
                            else if (rightIsInteger && !leftIsArray) {
                                try {
                                    expression[accumulated / 2 - 1] = new AdditionExpression(leftExpression.resultTo(rightExpression.resultingType()), rightExpression);
                                }
                                catch (TypeMismatchException e) {
                                    throwSemanticError(e.getMessage(), token);
                                }
                            }
                            else {
                                throwSemanticError("Unable to determine if '+' is a sum of 2 integers or a concatenation of arrays", operator[accumulated / 2 - 1]);
                            }

                            accumulated -= 2;
                        }
                        else if (operatorText.equals("-")) {
                            try {
                                expression[accumulated / 2 - 1] = new SubtractionExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }

                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("==")) {
                            expression[accumulated / 2 - 1] = new EqualThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("!=")) {
                            expression[accumulated / 2 - 1] = new DifferentFromExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">=")) {
                            try {
                                expression[accumulated / 2 - 1] = new GreaterOrEqualThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<=")) {
                            try {
                                expression[accumulated / 2 - 1] = new LowerOrEqualThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">")) {
                            try {
                                expression[accumulated / 2 - 1] = new GreaterThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<")) {
                            try {
                                expression[accumulated / 2 - 1] = new LowerThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new AndExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("|")) {
                            expression[accumulated / 2 - 1] = new OrExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        throw new IllegalStateException("Something went wrong");
                    }

                    if (assigningName != null) {
                        if (tokenText.equals(";")) {
                            assignments.append(new ConstantDefinitionStatement(assigningName, expression[0]));
                            assigningName = null;
                            accumulated = 0;
                        }
                        else {
                            throwSemanticError("Expected ';'", token);
                        }
                    }
                    else if (assignments.isEmpty()) {
                        return new ExpressionInterpretationResult(expression[0], token);
                    }
                    else {
                        return new ExpressionInterpretationResult(new ComplexExpression(assignments.toImmutable(), expression[0]), token);
                    }
                }
            }
            else if (tokenText.equals("(")) {
                if (accumulated == 0) {
                    final ExpressionInterpretationResult result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                    if (result.result instanceof ReferenceExpression refExp && result.closingToken.getText().equals(":")) {
                        final ImmutableList.Builder<FunctionParameter> parametersBuilder = new ImmutableList.Builder<>();
                        parametersBuilder.append(new FunctionParameter(refExp.getReference(), interpretType(knownTypes)));
                        Token separatorToken;
                        while ((separatorToken = nextTokenOrThrow("Expected ',' or ')")).getText().equals(",")) {
                            final Token paramNameToken = nextTokenOrThrow("Expected function parameter name");
                            validateNextToken(":", "Expected ':'");
                            parametersBuilder.append(new FunctionParameter(paramNameToken, interpretType(knownTypes)));
                        }

                        if (separatorToken.getText().equals(")")) {
                            validateNextToken("->", "Expected '->'");
                            final ExpressionInterpretationResult funcExpressionResult = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                            final Expression functionExpression = new FunctionExpression(parametersBuilder.build(), funcExpressionResult.result);
                            if (assigningName == null) {
                                return new ExpressionInterpretationResult(functionExpression, funcExpressionResult.closingToken);
                            }
                            else if (funcExpressionResult.closingToken.getText().equals(";")) {
                                assignments.append(new ConstantDefinitionStatement(assigningName, functionExpression));
                                assigningName = null;
                            }
                            else {
                                throwSemanticError("Expected ';'", token);
                            }
                        }
                        else {
                            throwSemanticError("Expected ',' or ')'", separatorToken);
                        }
                    }
                    else if (result.closingToken.getText().equals(")")) {
                        expression[0] = result.result;
                        accumulated = 1;
                    }
                    else {
                        throwSemanticError("Expected ')'", result.closingToken);
                    }
                }
                else if (accumulated % 2 == 0) {
                    final ExpressionInterpretationResult result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                    if (result.closingToken.getText().equals(")")) {
                        expression[accumulated / 2] = result.result;
                        accumulated++;
                    }
                    else {
                        throwSemanticError("Expected ')'", result.closingToken);
                    }
                }
                else if (accumulated % 2 == 1) {
                    final ImmutableList.Builder<Expression> parametersBuilder = new ImmutableList.Builder<>();
                    ExpressionInterpretationResult result;
                    while (!(result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments))).closingToken.getText().equals(")")) {
                        if (result.closingToken.getText().equals(",")) {
                            parametersBuilder.append(result.result);
                        }
                        else {
                            throwSemanticError("Expected ','", result.closingToken);
                        }
                    }

                    parametersBuilder.append(result.result);
                    expression[accumulated / 2] = new FunctionExecutionExpression(expression[accumulated / 2], parametersBuilder.build());
                }
                else {
                    throwSemanticError("Unexpected '('", token);
                }
            }
            else if (tokenText.equals("[")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else {
                    final ExpressionInterpretationResult result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                    if (result.closingToken.getText().equals("]")) {
                        if (expression[accumulated / 2] instanceof ReferenceExpression refExp && refExp.resultingType() == UnknownType.getInstance()) {
                            expression[accumulated / 2] = new ReferenceExpression(new ArrayType(UnknownType.getInstance()), refExp.getReference());
                        }

                        expression[accumulated / 2] = new ArrayValueAtExpression(expression[accumulated / 2], result.result);
                    }
                    else {
                        throwSemanticError("Expected ']'", token);
                    }
                }
            }
            else if (tokenText.equals("{")) {
                if (accumulated % 2 == 0) {
                    final ExpressionInterpretationResult result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                    if (result.closingToken.getText().equals("}")) {
                        expression[accumulated / 2] = result.result;
                        accumulated++;
                    }
                    else {
                        throwSemanticError("Expected '}'", result.closingToken);
                    }
                }
                else {
                    throwSemanticError("Expected operand", token);
                }
            }
            else if (tokenText.equals(".")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected reference before '.'", token);
                }
                else {
                    operator[accumulated / 2] = token;
                    accumulated++;
                }
            }
            else if (tokenText.equals("TRUE")) {
                if (accumulated == 0) {
                    expression[0] = new BooleanLiteralExpression(token, true);
                    accumulated = 1;
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (tokenText.equals("FALSE")) {
                if (accumulated == 0) {
                    expression[0] = new BooleanLiteralExpression(token, false);
                    accumulated = 1;
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (tokenText.equals("Array")) {
                validateNextToken("(", "Expected '('");
                final ImmutableList.Builder<Expression> paramsBuilder = new ImmutableList.Builder<>();
                ExpressionInterpretationResult paramExpressionResult;
                do {
                    paramExpressionResult = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                    paramsBuilder.append(paramExpressionResult.result);
                }
                while (paramExpressionResult.closingToken.getText().equals(","));

                if (paramExpressionResult.closingToken.getText().equals(")")) {
                    if (accumulated == 0) {
                        expression[0] = new ArrayConstructor(token, paramsBuilder.build());
                        accumulated = 1;
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throwSemanticError("Expected either ',' or ')'", paramExpressionResult.closingToken);
                }
            }
            else if (tokenText.charAt(0) >= 'A' && tokenText.charAt(0) <= 'Z') {
                if (knownTypes.containsKey(tokenText)) {
                    final Type type = knownTypes.get(tokenText);
                    if (type instanceof RegisterType regType) {
                        validateNextToken("{", "Expected '{'");
                        final ExpressionInterpretationResult result = interpretExpression(knownTypes, mergeKnownConstants(outerKnownConstants, assignments));
                        if (result.closingToken.getText().equals("}")) {
                            if (accumulated == 0) {
                                if (result.result instanceof ComplexExpression complexExpression) {
                                    if (complexExpression.getExpression() == null) {
                                        final ImmutableList<Statement> statements = complexExpression.getStatements();
                                        for (String fieldName : regType.getFields().keySet().map(Token::getText)) {
                                            if (!statements.anyMatch(st -> st instanceof ConstantDefinitionStatement assignment && assignment.getName().getText().equals(fieldName))) {
                                                throwSemanticError("Field '" + fieldName + "' not defined.", result.closingToken);
                                            }
                                        }

                                        expression[0] = new RegisterConstructor(regType, token, statements);
                                        accumulated = 1;
                                    }
                                    else {
                                        throwSemanticError("Unexpected expression within the Register construction", result.closingToken);
                                    }
                                }
                                else {
                                    throwSemanticError("No fields defined for register", result.closingToken);
                                }
                            }
                            else {
                                throw new UnsupportedOperationException("Unimplemented");
                            }
                        }
                        else {
                            throwSemanticError("Expected '}'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Type '" + tokenText + "' not expected here", token);
                    }
                }
                else {
                    throwSemanticError("Unknown type '" + tokenText + "'", token);
                }
            }
            else if (tokenText.charAt(0) == '"') {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Expected operator before expression", token);
                }
                else {
                    expression[accumulated / 2] = new StringLiteralExpression(token);
                    accumulated++;
                }
            }
            else if (tokenText.equals("if")) {
                if (accumulated == 0) {
                    final ImmutableMap<String, ConstantDefinitionStatement> mergedKnownConstants = mergeKnownConstants(outerKnownConstants, assignments);
                    final ExpressionInterpretationResult conditionResult = interpretExpression(knownTypes, mergedKnownConstants);
                    if (conditionResult.closingToken.getText().equals("then")) {
                        final ExpressionInterpretationResult thenClauseResult = interpretExpression(knownTypes, mergedKnownConstants);
                        if (thenClauseResult.closingToken.getText().equals("else")) {
                            final ExpressionInterpretationResult elseClauseResult = interpretExpression(knownTypes, mergedKnownConstants);
                            Expression thenClause = thenClauseResult.result;
                            Expression elseClause = elseClauseResult.result;
                            if (thenClause.resultingType() == UnknownType.getInstance()) {
                                if (elseClause.resultingType() != UnknownType.getInstance()) {
                                    try {
                                        thenClause = thenClause.resultTo(elseClause.resultingType());
                                    }
                                    catch (TypeMismatchException e) {
                                        throwSemanticError(e.getMessage(), token);
                                    }
                                }
                            }

                            if (elseClause.resultingType() == UnknownType.getInstance()) {
                                if (thenClause.resultingType() != UnknownType.getInstance()) {
                                    try {
                                        elseClause = elseClause.resultTo(thenClause.resultingType());
                                    }
                                    catch (TypeMismatchException e) {
                                        throwSemanticError(e.getMessage(), token);
                                    }
                                }
                            }

                            final IfExpression ifExpression = new IfExpression(conditionResult.result, thenClause, elseClause);
                            if (assigningName != null && elseClauseResult.closingToken.getText().equals(";")) {
                                assignments.append(new ConstantDefinitionStatement(assigningName, ifExpression));
                                assigningName = null;
                            }
                            else {
                                if (assignments.isEmpty()) {
                                    return new ExpressionInterpretationResult(ifExpression, elseClauseResult.closingToken);
                                }
                                else {
                                    return new ExpressionInterpretationResult(new ComplexExpression(assignments.toImmutable(), ifExpression), elseClauseResult.closingToken);
                                }
                            }
                        }
                        else {
                            throwSemanticError("Expected 'else'", thenClauseResult.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expected 'then'", conditionResult.closingToken);
                    }
                }
                else if (accumulated % 2 == 0) {
                    throw new UnsupportedOperationException("Unimplemented");
                }
                else {
                    throwSemanticError("Expected operator", token);
                }
            }
            else if (tokenText.equals("|")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else {
                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*")) {
                            expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("/")) {
                            expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("+")) {
                            expression[accumulated / 2 - 1] = new AdditionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new SubtractionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("==")) {
                            expression[accumulated / 2 - 1] = new EqualThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("!=")) {
                            expression[accumulated / 2 - 1] = new DifferentFromExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">=")) {
                            expression[accumulated / 2 - 1] = new GreaterOrEqualThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<=")) {
                            expression[accumulated / 2 - 1] = new LowerOrEqualThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">")) {
                            expression[accumulated / 2 - 1] = new GreaterThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<")) {
                            expression[accumulated / 2 - 1] = new LowerThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new AndExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("|")) {
                            expression[accumulated / 2 - 1] = new OrExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated != 1) {
                        throw new IllegalArgumentException("This code should not be reached");
                    }

                    operator[0] = token;
                    accumulated = 2;
                }
            }
            else if (tokenText.equals("&")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else {
                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*")) {
                            expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("/")) {
                            expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("+")) {
                            final Expression leftExpression = expression[accumulated / 2 - 1];
                            final Expression rightExpression = expression[accumulated / 2];
                            final boolean leftIsArray = leftExpression.resultingType() instanceof ArrayType;
                            final boolean leftIsInteger = leftExpression.resultingType() instanceof IntegerType;
                            final boolean rightIsArray = rightExpression.resultingType() instanceof ArrayType;
                            final boolean rightIsInteger = rightExpression.resultingType() instanceof IntegerType;

                            if (leftIsArray && !rightIsInteger || rightIsArray && !leftIsInteger) {
                                expression[accumulated / 2 - 1] = new ArrayConcatenationExpression(leftExpression, expression[accumulated / 2]);
                            }
                            else if (leftIsInteger && !rightIsArray || rightIsInteger && !leftIsArray) {
                                expression[accumulated / 2 - 1] = new AdditionExpression(leftExpression, expression[accumulated / 2]);
                            }
                            else {
                                throwSemanticError("Unable to determine if '+' is a sum of 2 integers or a concatenation of arrays", operator[accumulated / 2 - 1]);
                            }

                            accumulated -= 2;
                        }
                        else if (operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new SubtractionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("==")) {
                            expression[accumulated / 2 - 1] = new EqualThanExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("!=")) {
                            expression[accumulated / 2 - 1] = new DifferentFromExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">=")) {
                            try {
                                expression[accumulated / 2 - 1] = new GreaterOrEqualThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<=")) {
                            try {
                                expression[accumulated / 2 - 1] = new LowerOrEqualThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals(">")) {
                            try {
                                expression[accumulated / 2 - 1] = new GreaterThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("<")) {
                            try {
                                expression[accumulated / 2 - 1] = new LowerThanExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), expression[accumulated / 2].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new AndExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    operator[accumulated / 2] = token;
                    accumulated++;
                }
            }
            else if (tokenText.equals("==") || tokenText.equals("!=") || tokenText.equals(">=") || tokenText.equals(">") || tokenText.equals("<=") || tokenText.equals("<")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else {
                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*")) {
                            expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("/")) {
                            expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("+")) {
                            expression[accumulated / 2 - 1] = new AdditionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                        else if (operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new SubtractionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals("==") || operatorText.equals("!=") || operatorText.equals(">=") || operatorText.equals("<=") || operatorText.equals(">") || operatorText.equals("<")) {
                            throwSemanticError("Unexpected operator '" + tokenText + "' associated with previous operator '" + operatorText + "'", token);
                        }
                    }

                    operator[accumulated / 2] = token;
                    accumulated++;
                }
            }
            else if (tokenText.equals("+")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else if (accumulated == 1) {
                    operator[0] = token;
                    accumulated = 2;
                }
                else if (accumulated == 3) {
                    final String operatorText = operator[0].getText();
                    if (operatorText.equals("+")) {
                        final boolean leftIsArray = expression[0].resultingType() instanceof ArrayType;
                        final boolean leftIsInteger = expression[0].resultingType() instanceof IntegerType;
                        final boolean rightIsArray = expression[1].resultingType() instanceof ArrayType;
                        final boolean rightIsInteger = expression[1].resultingType() instanceof IntegerType;

                        if (leftIsArray && !rightIsInteger) {
                            try {
                                expression[0] = new ArrayConcatenationExpression(expression[0], expression[1].resultTo(expression[0].resultingType()));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                        }
                        else if (rightIsArray && !leftIsInteger) {
                            try {
                                expression[0] = new ArrayConcatenationExpression(expression[0].resultTo(expression[1].resultingType()), expression[1]);
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                        }
                        else if (leftIsInteger && !leftIsArray || rightIsInteger && !rightIsArray) {
                            try {
                                expression[0] = new AdditionExpression(expression[0].resultTo(TypeConstants.unboundIntegerType), expression[1].resultTo(TypeConstants.unboundIntegerType));
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                        }
                        else {
                            throwSemanticError("Unable to determine if '+' is a sum of 2 integers or a concatenation of arrays", operator[0]);
                        }

                        operator[0] = token;
                        accumulated = 2;
                    }
                    else {
                        throw new UnsupportedOperationException("'+' as second operator when first operator is '" + operatorText + "' is unimplemented");
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (tokenText.equals("-")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else if (accumulated == 1) {
                    operator[0] = token;
                    accumulated = 2;
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (tokenText.equals("*") || tokenText.equals("/") || tokenText.equals("%")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else if (accumulated == 1) {
                    operator[0] = token;
                    accumulated = 2;
                }
                else if (accumulated % 2 == 1) {
                    final String operatorText = operator[accumulated / 2 - 1].getText();
                    if (operatorText.equals(".")) {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                    else if (operatorText.equals("*")) {
                        expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                        operator[accumulated / 2 - 1] = token;
                        accumulated--;
                    }
                    else if (operatorText.equals("/")) {
                        expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                        operator[accumulated / 2 - 1] = token;
                        accumulated--;
                    }
                    else if (operatorText.equals("%")) {
                        expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1], expression[accumulated / 2]);
                        operator[accumulated / 2 - 1] = token;
                        accumulated--;
                    }
                    else {
                        operator[accumulated / 2] = token;
                        accumulated++;
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (tokenText.charAt(0) >= 'a' && tokenText.charAt(0) <= 'z') {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Operator expected", token);
                }
                else if (accumulated > 1 && operator[accumulated / 2 - 1].getText().equals(".")) {
                    expression[accumulated / 2 - 1] = new RegisterFieldAccessExpression(UnknownType.getInstance(), expression[accumulated / 2 - 1], token);
                    accumulated--;
                }
                else {
                    final ConstantDefinitionStatement constDef = mergeKnownConstants(outerKnownConstants, assignments).get(tokenText, null);
                    final Type refType = (constDef != null)? constDef.getExpression().resultingType() : UnknownType.getInstance();
                    expression[accumulated / 2] = new ReferenceExpression(refType, token);
                    accumulated++;
                }
            }
            else if (tokenText.charAt(0) >= '0' && tokenText.charAt(0) <= '9') {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Operator expected", token);
                }
                else {
                    final IntegerLiteralExpression intExpression = new IntegerLiteralExpression(token);
                    if (accumulated == 0) {
                        expression[0] = intExpression;
                        accumulated = 1;
                    }
                    else {
                        final String operatorText = operator[accumulated / 2 - 1].getText();
                        if (operatorText.equals(".")) {
                            throwSemanticError("Unexpected integer literal after '.'", token);
                        }
                        else if (operatorText.equals("*")) {
                            try {
                                expression[accumulated / 2 - 1] = new MultiplicationExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), intExpression);
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated--;
                        }
                        else if (operatorText.equals("/")) {
                            try {
                                expression[accumulated / 2 - 1] = new DivisionExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), intExpression);
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated--;
                        }
                        else if (operatorText.equals("%")) {
                            try {
                                expression[accumulated / 2 - 1] = new ModuleExpression(expression[accumulated / 2 - 1].resultTo(TypeConstants.unboundIntegerType), intExpression);
                            }
                            catch (TypeMismatchException e) {
                                throwSemanticError(e.getMessage(), token);
                            }
                            accumulated--;
                        }
                        else {
                            expression[accumulated / 2] = intExpression;
                            accumulated++;
                        }
                    }
                }
            }
            else if (tokenText.equals("=")) {
                if (assigningName == null && accumulated == 1 && expression[0] instanceof ReferenceExpression refExp) {
                    assigningName = refExp.getReference();
                    accumulated = 0;
                }
                else {
                    throwSemanticError("Unexpected operator '=' for assignment", token);
                }
            }
            else {
                throwSemanticError("Expected expression", token);
            }
        }
        while (true);
    }

    public ImmutableList<Statement> interpret() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        ImmutableMap<String, Type> knownTypes = new ImmutableHashMap.Builder<String, Type>()
                .put("Boolean", TypeConstants.booleanType)
                .put("String", new ArrayType(new IntegerType(new Token("0"), new Token("255"))))
                .build();

        ImmutableMap<String, ConstantDefinitionStatement> knownConstants = ImmutableHashMap.empty();

        final ImmutableList.Builder<Statement> builder = new ImmutableList.Builder<>();
        Token typeKeywordToken = null;
        Token assignmentName = null;
        Token token;
        while ((token = mParser.next()) != null) {
            final String tokenText = token.getText();
            if (typeKeywordToken == null) {
                if (assignmentName == null) {
                    if (tokenText.equals("type")) {
                        typeKeywordToken = token;
                    }
                    else if (tokenText.charAt(0) >= 'a' && tokenText.charAt(0) <= 'z') {
                        assignmentName = token;

                        if (knownConstants.containsKey(assignmentName.getText())) {
                            throwSemanticError("Constant \"" + assignmentName.getText() + "\" already declared in this scope", token);
                        }
                    }
                    else {
                        throwSemanticError("Constant names must start with a lower-case character", token);
                    }
                }
                else {
                    if (tokenText.equals("=")) {
                        final ExpressionInterpretationResult result = interpretExpression(knownTypes, knownConstants);
                        if (result.closingToken.getText().equals(";")) {
                            final ConstantDefinitionStatement statement = new ConstantDefinitionStatement(assignmentName, result.result);
                            knownConstants = knownConstants.put(assignmentName.getText(), statement);
                            builder.append(statement);
                            assignmentName = null;
                        }
                        else {
                            throwSemanticError("Expected ';'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expected '=' after constant name", token);
                    }
                }
            }
            else {
                if (assignmentName == null) {
                    if (tokenText.charAt(0) >= 'A' && tokenText.charAt(0) <= 'Z') {
                        assignmentName = token;
                    }
                    else {
                        throwSemanticError("Type names must start with an upper-case character", token);
                    }
                }
                else {
                    if (tokenText.equals("=")) {
                        final Type newType = interpretType(knownTypes);
                        builder.append(new TypeAliasStatement(assignmentName, newType));
                        knownTypes = knownTypes.put(assignmentName.getText(), newType);
                        final Token semicolonToken = nextTokenOrThrow("Expected ';' after type definition");
                        if (semicolonToken.getText().equals(";")) {
                            typeKeywordToken = null;
                            assignmentName = null;
                        }
                        else {
                            throwSemanticError("Expected ';' after type definition", semicolonToken);
                        }
                    }
                    else {
                        throwSemanticError("Expected '=' after type name", token);
                    }
                }
            }
        }

        if (assignmentName != null) {
            final String assignmentNameText = assignmentName.getText();
            final String item = (typeKeywordToken == null)? "constant" : "type";
            throw new UnexpectedEndOfFileException("Expected " + item + " '" + assignmentNameText + "' definition");
        }
        else if (typeKeywordToken != null) {
            throw new UnexpectedEndOfFileException("Expected type definition");
        }

        return builder.build();
    }
}
