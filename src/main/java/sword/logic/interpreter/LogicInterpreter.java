package sword.logic.interpreter;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.MutableHashMap;
import sword.collections.MutableList;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.SyntaxErrorException;
import sword.logic.compiler.TokenParser;
import sword.logic.compiler.UnexpectedEndOfFileException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.expressions.ArrayConstructionExpression;
import sword.logic.interpreter.expressions.ArrayValueAtExpression;
import sword.logic.interpreter.expressions.ComplexExpression;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.expressions.FunctionDefinitionExpression;
import sword.logic.interpreter.expressions.FunctionExecutionExpression;
import sword.logic.interpreter.expressions.IfExpression;
import sword.logic.interpreter.expressions.LeftRightExpression;
import sword.logic.interpreter.expressions.LiteralExpression;
import sword.logic.interpreter.expressions.ReferenceExpression;
import sword.logic.interpreter.expressions.RegisterConstructionExpression;
import sword.logic.interpreter.expressions.RegisterFieldAccessExpression;
import sword.logic.interpreter.scopes.BuiltInScope;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.statements.ConstantDefinitionStatement;
import sword.logic.interpreter.statements.Statement;
import sword.logic.interpreter.statements.TypeDefinitionStatement;
import sword.logic.interpreter.type.definitions.ArrayTypeDefinition;
import sword.logic.interpreter.type.definitions.FunctionParameter;
import sword.logic.interpreter.type.definitions.IntTypeDefinition;
import sword.logic.interpreter.type.definitions.ReferenceTypeDefinition;
import sword.logic.interpreter.type.definitions.RegisterTypeDefinition;
import sword.logic.interpreter.type.definitions.TypeDefinition;
import sword.logic.interpreter.type.definitions.TypeMention;
import sword.logic.types.Type;
import sword.logic.types.TypeConstants;

import java.io.IOException;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;

public final class LogicInterpreter {

    private final TokenParser mParser;

    public LogicInterpreter(TokenParser parser) {
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

    private static final class TypeMentionInterpretationResult {
        final TypeMention typeMention;
        final Token closingToken;

        TypeMentionInterpretationResult(TypeMention typeMention, Token closingToken) {
            ensureNonNull(typeMention, closingToken);
            this.typeMention = typeMention;
            this.closingToken = closingToken;
        }
    }

    private TypeMentionInterpretationResult interpretTypeMention() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        final Token typeToken = nextTokenOrThrow("Expected type");
        final String typeTokenText = typeToken.getText();
        if (typeTokenText.equals(TypeConstants.INTEGER_TYPE_TEXT)) {
            validateNextToken("[", "Expected '[' after 'Int'");
            final Token minToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("..", "Expected '..'");
            final Token maxToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("]", "Expected ']'");
            final Token newToken = new Token(typeToken.getLine(), typeToken.getColumn(), TypeConstants.INTEGER_TYPE_TEXT);
            final IntTypeDefinition definition = new IntTypeDefinition(newToken, minToken, maxToken);
            // TODO: Check if the given token is not ';', like in enums of types
            return new TypeMentionInterpretationResult(definition, mParser.next());
        }
        else if (typeTokenText.equals(TypeConstants.ARRAY_TYPE_TEXT)) {
            validateNextToken("[", "Expected '[' after 'Array'");
            final TypeMentionInterpretationResult itemResult = interpretTypeMention();
            if (itemResult.closingToken.getText().equals("]")) {
                final Token newToken = new Token(typeToken.getLine(), typeToken.getColumn(), TypeConstants.ARRAY_TYPE_TEXT);
                final ArrayTypeDefinition definition = new ArrayTypeDefinition(newToken, itemResult.typeMention);
                // TODO: Check if the given token is not ';', like in enums of types
                return new TypeMentionInterpretationResult(definition, mParser.next());
            }
            else {
                throwSemanticError("Expected ']'", itemResult.closingToken);
            }
        }
        else if (validTypeName(typeTokenText)) {
            final ReferenceTypeDefinition definition = new ReferenceTypeDefinition(typeToken);
            // TODO: Check if the given token is not ';', like in enums of types
            return new TypeMentionInterpretationResult(definition, mParser.next());
        }
        else {
            throwSemanticError("Unknown type '" + typeTokenText + "'", typeToken);
        }

        throw new IllegalStateException("This code should be unreachable");
    }

    private static final class TypeDefinitionInterpretationResult {
        final TypeDefinition typeDefinition;
        final Token closingToken;

        TypeDefinitionInterpretationResult(TypeDefinition typeDefinition, Token closingToken) {
            ensureNonNull(typeDefinition, closingToken);
            this.typeDefinition = typeDefinition;
            this.closingToken = closingToken;
        }
    }

    private TypeDefinitionInterpretationResult interpretTypeDefinition() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        final Token typeToken = nextTokenOrThrow("Expected type");
        final String typeTokenText = typeToken.getText();
        if (typeTokenText.equals(TypeConstants.INTEGER_TYPE_TEXT)) {
            validateNextToken("[", "Expected '[' after 'Int'");
            final Token minToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("..", "Expected '..'");
            final Token maxToken = nextTokenOrThrow("Expected integer literal");
            validateNextToken("]", "Expected ']'");
            final Token newToken = new Token(typeToken.getLine(), typeToken.getColumn(), TypeConstants.INTEGER_TYPE_TEXT);
            final IntTypeDefinition definition = new IntTypeDefinition(newToken, minToken, maxToken);
            // TODO: Check if the given token is not ';', like in enums of types
            return new TypeDefinitionInterpretationResult(definition, mParser.next());
        }
        else if (typeTokenText.equals(TypeConstants.ARRAY_TYPE_TEXT)) {
            validateNextToken("[", "Expected '[' after 'Array'");
            final TypeMentionInterpretationResult itemResult = interpretTypeMention();
            if (itemResult.closingToken.getText().equals("]")) {
                final Token newToken = new Token(typeToken.getLine(), typeToken.getColumn(), TypeConstants.ARRAY_TYPE_TEXT);
                final ArrayTypeDefinition definition = new ArrayTypeDefinition(newToken, itemResult.typeMention);
                // TODO: Check if the given token is not ';', like in enums of types
                return new TypeDefinitionInterpretationResult(definition, mParser.next());
            }
            else {
                throwSemanticError("Expected ']'", itemResult.closingToken);
            }
        }
        else if (typeTokenText.equals("{")) {
            final ImmutableList.Builder<FunctionParameter> parametersBuilder = new ImmutableList.Builder<>();
            Token nameToken = nextTokenOrThrow("Expected property name for register");
            if (nameToken.getText().equals("}")) {
                throwSemanticError("Empty register definition", nameToken);
            }
            else {
                while (!nameToken.getText().equals("}")) {
                    if (validConstantName(nameToken.getText())) {
                        validateNextToken(":", "Expected ':' after name");
                        final TypeMentionInterpretationResult fieldTypeResult = interpretTypeMention();
                        if (fieldTypeResult.closingToken.getText().equals(";")) {
                            parametersBuilder.append(new FunctionParameter(nameToken, fieldTypeResult.typeMention));
                            nameToken = nextTokenOrThrow("Expected property name for register, or '}'");
                        }
                        else {
                            throwSemanticError("Expected ';' after type definition", fieldTypeResult.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Register field names must start with a lower-case character", nameToken);
                    }
                }

                final RegisterTypeDefinition definition = new RegisterTypeDefinition(parametersBuilder.build());
                // TODO: Check if the given token is not ';', like in enums of types
                return new TypeDefinitionInterpretationResult(definition, mParser.next());
            }
        }
        else if (validTypeName(typeTokenText)) {
            final ReferenceTypeDefinition definition = new ReferenceTypeDefinition(typeToken);
            // TODO: Check if the given token is not ';', like in enums of types
            return new TypeDefinitionInterpretationResult(definition, mParser.next());
        }
        else {
            throwSemanticError("Unknown type '" + typeTokenText + "'", typeToken);
        }

        throw new IllegalStateException("This code should be unreachable");
    }

    private static final class ExpressionInterpretationResult {
        final Interpretation result;
        final Token closingToken;

        ExpressionInterpretationResult(Interpretation result, Token closingToken) {
            ensureNonNull(result, closingToken);
            this.result = result;
            this.closingToken = closingToken;
        }
    }

    private ExpressionInterpretationResult interpretExpression() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
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
            if (tokenText.equals(",") || tokenText.equals(":") || tokenText.equals(";") || tokenText.equals(")") || tokenText.equals("]") || tokenText.equals("}") || tokenText.equals(Keywords.THEN) || tokenText.equals(Keywords.ELSE)) {
                if (accumulated == 0 && !assignments.isEmpty()) {
                    return new ExpressionInterpretationResult(new StatementSetInterpretation(assignments.toImmutable()), token);
                }
                else if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression", token);
                }
                else {
                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("+") || operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("==") || operatorText.equals("!=") || operatorText.equals(">=") || operatorText.equals("<=") || operatorText.equals(">") || operatorText.equals("<")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("|")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
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
                    else {
                        final Expression result = assignments.isEmpty()? expression[0] :
                                new ComplexExpression(assignments.toImmutable(), expression[0]);
                        return new ExpressionInterpretationResult(result, token);
                    }
                }
            }
            else if (tokenText.equals("(")) {
                if (accumulated == 0) {
                    final ExpressionInterpretationResult result = interpretExpression();
                    if (result.result instanceof ReferenceExpression refExp && result.closingToken.getText().equals(":")) {
                        final ImmutableList.Builder<FunctionParameter> parametersBuilder = new ImmutableList.Builder<>();
                        TypeMentionInterpretationResult mentionResult = interpretTypeMention();
                        parametersBuilder.append(new FunctionParameter(refExp.getReference(), mentionResult.typeMention));
                        while (mentionResult.closingToken.getText().equals(",")) {
                            final Token paramNameToken = nextTokenOrThrow("Expected function parameter name");
                            validateNextToken(":", "Expected ':'");
                            mentionResult = interpretTypeMention();
                            parametersBuilder.append(new FunctionParameter(paramNameToken, mentionResult.typeMention));
                        }

                        if (mentionResult.closingToken.getText().equals(")")) {
                            validateNextToken("->", "Expected '->'");
                            final ImmutableList<FunctionParameter> parameters = parametersBuilder.build();
                            final ExpressionInterpretationResult funcExpressionResult = interpretExpression();
                            if (funcExpressionResult.result instanceof Expression resultExp) {
                                final Expression functionExpression = new FunctionDefinitionExpression(parameters, resultExp);
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
                                throwSemanticError("Expression expected as function body", funcExpressionResult.closingToken);
                            }
                        }
                        else {
                            throwSemanticError("Expected ',' or ')'", mentionResult.closingToken);
                        }
                    }
                    else if (result.result instanceof Expression resultExp) {
                        if (result.closingToken.getText().equals(")")) {
                            expression[0] = resultExp;
                            accumulated = 1;
                        }
                        else {
                            throwSemanticError("Expected ')'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected", result.closingToken);
                    }
                }
                else if (accumulated % 2 == 0) {
                    final ExpressionInterpretationResult result = interpretExpression();
                    if (result.result instanceof Expression resultExp) {
                        if (result.closingToken.getText().equals(")")) {
                            expression[accumulated / 2] = resultExp;
                            accumulated++;
                        }
                        else {
                            throwSemanticError("Expected ')'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected", result.closingToken);
                    }
                }
                else if (accumulated % 2 == 1) {
                    final ImmutableList.Builder<Expression> parametersBuilder = new ImmutableList.Builder<>();
                    ExpressionInterpretationResult result;
                    while (!(result = interpretExpression()).closingToken.getText().equals(")")) {
                        if (result.result instanceof Expression resultExp) {
                            if (result.closingToken.getText().equals(",")) {
                                parametersBuilder.append(resultExp);
                            }
                            else {
                                throwSemanticError("Expected ','", result.closingToken);
                            }
                        }
                        else {
                            throwSemanticError("Expression expected as function parameter", result.closingToken);
                        }
                    }

                    if (result.result instanceof Expression resultExp) {
                        parametersBuilder.append(resultExp);
                        expression[accumulated / 2] = new FunctionExecutionExpression(token, expression[accumulated / 2], parametersBuilder.build());
                    }
                    else {
                        throwSemanticError("Expression expected as function parameter", result.closingToken);
                    }
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
                    final ExpressionInterpretationResult result = interpretExpression();
                    if (result.result instanceof Expression resultExp) {
                        if (result.closingToken.getText().equals("]")) {
                            expression[accumulated / 2] = new ArrayValueAtExpression(token, result.closingToken, expression[accumulated / 2], resultExp);
                        }
                        else {
                            throwSemanticError("Expected ']'", token);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected as array index", result.closingToken);
                    }
                }
            }
            else if (tokenText.equals("{")) {
                if (accumulated % 2 == 0) {
                    final ExpressionInterpretationResult result = interpretExpression();
                    if (result.result instanceof Expression resultExp) {
                        if (result.closingToken.getText().equals("}")) {
                            expression[accumulated / 2] = resultExp;
                            accumulated++;
                        }
                        else {
                            throwSemanticError("Expected '}'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected", result.closingToken);
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
            else if (tokenText.equals("TRUE") || tokenText.equals("FALSE") || tokenText.charAt(0) == '"' && tokenText.charAt(tokenText.length() - 1) == '"') {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Operator expected", token);
                }
                else {
                    if (accumulated > 1 && operator[accumulated / 2 - 1].getText().equals(".")) {
                        throwSemanticError("Unexpected literal expression after operator '.'", token);
                    }

                    expression[accumulated / 2] = new LiteralExpression(token);
                    accumulated++;
                }
            }
            else if (tokenText.equals(TypeConstants.ARRAY_TYPE_TEXT)) {
                validateNextToken("(", "Expected '('");
                final ImmutableList.Builder<Expression> paramsBuilder = new ImmutableList.Builder<>();
                ExpressionInterpretationResult paramExpressionResult;
                do {
                    paramExpressionResult = interpretExpression();
                    if (paramExpressionResult.result instanceof Expression resultExp) {
                        paramsBuilder.append(resultExp);
                    }
                    else {
                        throwSemanticError("Expression expected as array parameter", paramExpressionResult.closingToken);
                    }
                }
                while (paramExpressionResult.closingToken.getText().equals(","));

                if (paramExpressionResult.closingToken.getText().equals(")")) {
                    if (accumulated == 0) {
                        final Token newToken = new Token(token.getLine(), token.getColumn(), TypeConstants.ARRAY_TYPE_TEXT);
                        expression[0] = new ArrayConstructionExpression(newToken, paramsBuilder.build());
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
            else if (validTypeName(tokenText)) {
                validateNextToken("{", "Expected '{'");
                final ExpressionInterpretationResult result = interpretExpression();
                if (result.closingToken.getText().equals("}")) {
                    if (accumulated == 0) {
                        if (result.result instanceof StatementSetInterpretation statementSet) {
                            final ImmutableList<Statement> statements = statementSet.getStatements();
                            expression[0] = new RegisterConstructionExpression(token, statements);
                            accumulated = 1;
                        }
                        else {
                            throwSemanticError("Invalid register construction", result.closingToken);
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
            else if (tokenText.equals(Keywords.IF)) {
                if (accumulated == 0) {
                    final ExpressionInterpretationResult conditionResult = interpretExpression();
                    if (conditionResult.result instanceof Expression conditionExp) {
                        if (conditionResult.closingToken.getText().equals(Keywords.THEN)) {
                            final ExpressionInterpretationResult thenClauseResult = interpretExpression();
                            if (thenClauseResult.result instanceof Expression thenExp) {
                                if (thenClauseResult.closingToken.getText().equals(Keywords.ELSE)) {
                                    final ExpressionInterpretationResult elseClauseResult = interpretExpression();
                                    if (elseClauseResult.result instanceof Expression elseExp) {
                                        final Token newIfToken = new Token(token.getLine(), token.getColumn(), Keywords.IF);
                                        final Token newThenToken = new Token(conditionResult.closingToken.getLine(), conditionResult.closingToken.getColumn(), Keywords.THEN);
                                        final Token newElseToken = new Token(thenClauseResult.closingToken.getLine(), thenClauseResult.closingToken.getColumn(), Keywords.ELSE);
                                        final IfExpression ifExpression = new IfExpression(newIfToken, newThenToken, newElseToken, conditionExp, thenExp, elseExp);
                                        if (assigningName != null && elseClauseResult.closingToken.getText().equals(";")) {
                                            assignments.append(new ConstantDefinitionStatement(assigningName, ifExpression));
                                            assigningName = null;
                                        }
                                        else {
                                            final Expression resultExpression = assignments.isEmpty() ? ifExpression : new ComplexExpression(assignments.toImmutable(), ifExpression);
                                            return new ExpressionInterpretationResult(resultExpression, elseClauseResult.closingToken);
                                        }
                                    }
                                    else {
                                        throwSemanticError("Expression expected in else clause", elseClauseResult.closingToken);
                                    }
                                }
                                else {
                                    throwSemanticError("Expected 'else'", thenClauseResult.closingToken);
                                }
                            }
                            else {
                                throwSemanticError("Expression expected in then clause", thenClauseResult.closingToken);
                            }
                        }
                        else {
                            throwSemanticError("Expected 'then'", conditionResult.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected as if-condition", conditionResult.closingToken);
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
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("+") || operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("==") || operatorText.equals("!=") || operatorText.equals(">=") || operatorText.equals("<=") || operatorText.equals(">") || operatorText.equals("<")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("|")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
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
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("+") || operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("==") || operatorText.equals("!=") || operatorText.equals(">=") || operatorText.equals("<=") || operatorText.equals(">") || operatorText.equals("<")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("&")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
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
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals(".")) {
                            throw new IllegalStateException("This code should not be reached");
                        }

                        if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                            accumulated -= 2;
                        }
                    }

                    if (accumulated > 1) {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals("+") || operatorText.equals("-")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
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
            else if (tokenText.equals("+") || tokenText.equals("-")) {
                if (accumulated % 2 == 0) {
                    throwSemanticError("Expected expression before operator", token);
                }
                else if (accumulated == 1) {
                    operator[0] = token;
                    accumulated = 2;
                }
                else {
                    final Token lastOperator = operator[accumulated / 2 - 1];
                    final String operatorText = lastOperator.getText();
                    if (operatorText.equals("+") || operatorText.equals("-")) {
                        expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                        operator[0] = token;
                        accumulated = 2;
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
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
                else {
                    final Token lastOperator = operator[accumulated / 2 - 1];
                    final String operatorText = lastOperator.getText();
                    if (operatorText.equals(".")) {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                    else if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                        expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], expression[accumulated / 2]);
                        operator[accumulated / 2 - 1] = token;
                        accumulated--;
                    }
                    else {
                        operator[accumulated / 2] = token;
                        accumulated++;
                    }
                }
            }
            else if (validConstantName(tokenText)) {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Operator expected", token);
                }
                else if (accumulated > 1 && operator[accumulated / 2 - 1].getText().equals(".")) {
                    expression[accumulated / 2 - 1] = new RegisterFieldAccessExpression(operator[accumulated / 2 - 1], expression[accumulated / 2 - 1], token);
                    accumulated--;
                }
                else {
                    expression[accumulated / 2] = new ReferenceExpression(token);
                    accumulated++;
                }
            }
            else if (tokenText.charAt(0) >= '0' && tokenText.charAt(0) <= '9') {
                if (accumulated % 2 == 1) {
                    throwSemanticError("Operator expected", token);
                }
                else {
                    final LiteralExpression intExpression = new LiteralExpression(token);
                    if (accumulated == 0) {
                        expression[0] = intExpression;
                        accumulated = 1;
                    }
                    else {
                        final Token lastOperator = operator[accumulated / 2 - 1];
                        final String operatorText = lastOperator.getText();
                        if (operatorText.equals(".")) {
                            throwSemanticError("Unexpected integer literal after '.'", token);
                        }
                        else if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%")) {
                            expression[accumulated / 2 - 1] = new LeftRightExpression(lastOperator, expression[accumulated / 2 - 1], intExpression);
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

    private ImmutableList<Statement> obtainSyntaxTree() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException {
        ImmutableSet<String> knownConstants = ImmutableHashSet.empty();
        ImmutableSet<String> knownTypes = ImmutableHashSet.empty();
        final ImmutableList.Builder<Statement> builder = new ImmutableList.Builder<>();
        Token typeKeywordToken = null;
        Token assignmentName = null;
        Token token;
        while ((token = mParser.next()) != null) {
            final String tokenText = token.getText();
            if (typeKeywordToken == null) {
                if (assignmentName == null) {
                    if (tokenText.equals(Keywords.TYPE)) {
                        typeKeywordToken = token;
                    }
                    else if (validConstantName(tokenText)) {
                        if (knownConstants.contains(tokenText)) {
                            throwSemanticError("Constant \"" + tokenText + "\" already declared in this scope", token);
                        }
                        else {
                            assignmentName = token;
                        }
                    }
                    else {
                        throwSemanticError("Constant names must start with a lower-case character", token);
                    }
                }
                else if (tokenText.equals("=")) {
                    final ExpressionInterpretationResult result = interpretExpression();
                    if (result.result instanceof Expression resultExp) {
                        if (result.closingToken.getText().equals(";")) {
                            builder.append(new ConstantDefinitionStatement(assignmentName, resultExp));
                            knownConstants = knownConstants.add(assignmentName.getText());
                            assignmentName = null;
                        }
                        else {
                            throwSemanticError("Expected ';'", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expression expected as constant definition", result.closingToken);
                    }
                }
                else {
                    throwSemanticError("Expected '=' after constant name", token);
                }
            }
            else {
                if (assignmentName == null) {
                    if (validTypeName(tokenText)) {
                        if (knownTypes.contains(tokenText)) {
                            throwSemanticError("Duplicated type name", token);
                        }
                        else {
                            assignmentName = token;
                        }
                    }
                    else {
                        throwSemanticError("Type names must start with an upper-case character", token);
                    }
                }
                else {
                    if (tokenText.equals("=")) {
                        final TypeDefinitionInterpretationResult result = interpretTypeDefinition();
                        if (result.closingToken.getText().equals(";")) {
                            builder.append(new TypeDefinitionStatement(assignmentName, result.typeDefinition));
                            knownTypes = knownTypes.add(assignmentName.getText());
                            typeKeywordToken = null;
                            assignmentName = null;
                        }
                        else {
                            throwSemanticError("Expected ';' after type definition", result.closingToken);
                        }
                    }
                    else {
                        throwSemanticError("Expected '=' after type name", token);
                    }
                }
            }
        }

        return builder.build();
    }

    private ImmutableMap<Finder, Scope> obtainScopeMap(ImmutableList<Statement> statements) {
        final MutableMap<Finder, Scope> scopeMap = MutableHashMap.empty();

        // TODO: All these built in types uses Token that points to nothing... we should rethink this
        final BuiltInScope builtInScope = BuiltInScope.getInstance();
        final Scope rootScope = builtInScope.createWithStatements(statements);
        for (Statement statement : statements) {
            statement.findAllFinders(scopeMap, rootScope);
        }

        return scopeMap.toImmutable();
    }

    private ImmutableMap<Expression, Type> obtainTypeExpressions(ImmutableMap<Finder, Scope> scopeMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, UnresolvedEnumValueException, SemanticErrorException {
        final ImmutableSet<Expression> allExpressions = scopeMap.keySet().filter(k -> k instanceof Expression).map(k -> (Expression) k).toSet();
        final MutableMap<Expression, Type> typedExpressions = MutableHashMap.empty();

        int lastResolved;
        do {
            lastResolved = typedExpressions.size();

            for (Expression expression : allExpressions.filterNot(typedExpressions::containsKey).toImmutable()) {
                final Type type = expression.resolveType(scopeMap.get(expression), typedExpressions);
                if (type != null) {
                    typedExpressions.put(expression, type);
                }
            }

            if (typedExpressions.size() == lastResolved) {
                throw new RuntimeException("Unable to resolve the type for " + (scopeMap.size() - typedExpressions.size()) + " out of " + typedExpressions.size());
            }
        }
        while (typedExpressions.size() < allExpressions.size());

        return typedExpressions.toImmutable();
    }

    public Result interpret() throws IOException, SyntaxErrorException, SemanticErrorException, UnexpectedEndOfFileException, UnresolvedTypeReferenceException, UnresolvedEnumValueException, UnresolvedReferenceException {
        final ImmutableList<Statement> statements = obtainSyntaxTree();
        final ImmutableMap<Finder, Scope> scopeMap = obtainScopeMap(statements);
        final ImmutableMap<Expression, Type> resolvedExpressions = obtainTypeExpressions(scopeMap);
        final MutableMap<Expression, sword.logic.expressions.Expression> expressionMap = MutableHashMap.empty();
        final ImmutableList<sword.logic.statements.Statement> resultStatements = statements.map(st -> st.untokenize(scopeMap, resolvedExpressions, expressionMap));
        final ImmutableSet<Expression> allExpressions = resolvedExpressions.keySet();
        ensureValidState(allExpressions.equalSet(expressionMap.keySet()));

        final ImmutableMap.Builder<sword.logic.expressions.Expression, Type> resultTypeMapBuilder = new ImmutableHashMap.Builder<>();
        for (Expression exp : allExpressions) {
            resultTypeMapBuilder.put(expressionMap.get(exp), resolvedExpressions.get(exp));
        }

        return new Result(resultStatements, resultTypeMapBuilder.build());
    }

    public static final class Result {
        private final ImmutableList<sword.logic.statements.Statement> mStatements;
        private final ImmutableMap<sword.logic.expressions.Expression, Type> mExpressionTypeMap;

        Result(ImmutableList<sword.logic.statements.Statement> statements, ImmutableMap<sword.logic.expressions.Expression, Type> expressionTypeMap) {
            ensureNonNull(statements, expressionTypeMap);
            mStatements = statements;
            mExpressionTypeMap = expressionTypeMap;
        }

        public ImmutableList<sword.logic.statements.Statement> getStatements() {
            return mStatements;
        }

        public ImmutableMap<sword.logic.expressions.Expression, Type> getExpressionTypeMap() {
            return mExpressionTypeMap;
        }
    }
}
