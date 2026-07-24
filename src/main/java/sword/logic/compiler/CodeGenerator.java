package sword.logic.compiler;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.collections.MutableHashMap;
import sword.collections.MutableHashSet;
import sword.collections.MutableMap;
import sword.collections.MutableSet;
import sword.collections.MutableSetExtensions;
import sword.collections.Predicate;
import sword.collections.Procedure;
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
import sword.logic.syntax_tree.expressions.Expression.WarningMessage;
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
import sword.logic.syntax_tree.types.TypeConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static sword.logic.compiler.IntegerLiteralOperations.greaterOrEqualThan;
import static sword.logic.compiler.IntegerLiteralOperations.lowerThan;
import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CodeGenerator {

    private static final String INDENTATION = "  ";
    private static final String OUT_RESULT = "outResult";
    private static final String PLACEHOLDER = "<To be implemented>";

    private static final class CVariable {
        final String name;
        final String type;

        CVariable(String name, String type) {
            ensureValidArguments(!name.isEmpty() && !type.isEmpty());
            this.name = name;
            this.type = type;
        }
    }

    private static final class CFunction {
        final String name;
        final ImmutableList<CVariable> params;
        final String returnType;
        final Expression body;
        final boolean isStatic;
        final boolean firstParamIsOutput;

        CFunction(String name, ImmutableList<CVariable> params, String returnType, Expression body, boolean isStatic, boolean firstParamIsOutput) {
            ensureValidArguments(!name.isEmpty() && params != null && !returnType.isEmpty());
            ensureValidArguments(params.map(param -> param.name).toSet().size() == params.size());
            ensureValidArguments(!firstParamIsOutput || !params.isEmpty());
            this.name = name;
            this.params = params;
            this.returnType = returnType;
            this.body = body;
            this.isStatic = isStatic;
            this.firstParamIsOutput = firstParamIsOutput;
        }
    }

    private static final class CStruct {
        final String name;
        final ImmutableList<CVariable> fields;

        CStruct(String name, ImmutableList<CVariable> fields) {
            ensureNonNull(name, fields);
            ensureValidArguments(!fields.isEmpty());
            this.name = name;
            this.fields = fields;
        }
    }

    private String cType(Type type) {
        if (type instanceof IntegerType intType) {
            final String minText = intType.getMin().getText();
            final String maxText = intType.getMax().getText();
            if (minText.equals(TypeConstants.unboundText) || maxText.equals(TypeConstants.unboundText)) {
                return "int";
            }
            else if (greaterOrEqualThan(minText, "-128") && lowerThan(maxText, "128")) {
                return "char";
            }
            else if (greaterOrEqualThan(minText, "0") && lowerThan(maxText, "256")) {
                return "unsigned char";
            }
            else {
                return "int";
            }
        }
        else if (type instanceof ArrayType) {
            return "struct Array";
        }
        else if (type == TypeConstants.booleanType) {
            return "int";
        }
        else if (type instanceof RegisterType regType) {
            return "struct " + regType.getName().getText();
        }
        else {
            return "void";
        }
    }

    private boolean areParenthesesRequiredOnMultiplication(Expression expression) {
        return !(expression instanceof IntegerLiteralExpression ||
                expression instanceof ReferenceExpression ||
                expression instanceof MultiplicationExpression ||
                expression instanceof DivisionExpression ||
                expression instanceof ModuleExpression ||
                expression instanceof RegisterFieldAccessExpression ||
                expression instanceof ArrayValueAtExpression ||
                expression instanceof FunctionExecutionExpression);
    }

    private void dumpExpressionWrappingIfRequired(Predicate<Expression> areParenthesesRequiredFunction, Expression expression, StringBuilder sb, String indentation, VariableNameCreator varNameCreator, MutableSet<String> pointers, String outVarName, int outOffset) {
        final boolean areParenthesesRequired = areParenthesesRequiredFunction.apply(expression);
        if (areParenthesesRequired) {
            sb.append("(");
        }

        dumpExpression(expression, sb, indentation, varNameCreator, pointers, outVarName, outOffset);

        if (areParenthesesRequired) {
            sb.append(")");
        }
    }

    public void dumpExpression(Expression expression, StringBuilder sb, String indentation, VariableNameCreator varNameCreator, MutableSet<String> pointers, String outVarName, int outOffset) {
        final Procedure<WarningMessage> logger = msg -> {};
        if (expression instanceof AdditionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") + (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof AndExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") && (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof ArrayConcatenationExpression exp) {
            if (outVarName != null) {
                final ArrayType resultingType = (ArrayType) exp.resultingType(ImmutableHashMap.empty(), logger);
                if (resultingType.getLengthType().getMin().getText().equals(resultingType.getLengthType().getMax().getText())) {
                    final Expression leftExpression = exp.getLeftExpression();
                    final IntegerType leftLengthType = ((ArrayType) leftExpression.resultingType(ImmutableHashMap.empty(), logger)).getLengthType();
                    if (leftLengthType.getMin().getText().equals(leftLengthType.getMax().getText())) {
                        dumpExpression(leftExpression, sb, indentation, varNameCreator, pointers, outVarName, outOffset);
                        final int leftSize = Integer.parseInt(leftLengthType.getMin().getText());
                        dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, outVarName, outOffset + leftSize);
                    }
                    else {
                        sb.append(PLACEHOLDER);
                    }
                }
                else {
                    sb.append("???");
                }
            }
            else {
                sb.append(PLACEHOLDER);
            }
        }
        else if (expression instanceof ArrayConstructor exp) {
            sb.append("{");
            boolean separatorRequired = false;
            for (Expression value : exp.getValues()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                dumpExpression(value, sb, indentation, varNameCreator, pointers, null, 0);
                separatorRequired = true;
            }
            sb.append("}");
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            if (outVarName != null) {
                final Type resultingType = exp.resultingType(ImmutableHashMap.empty(), logger);;
                if (resultingType instanceof ArrayType arrayResultingType) {
                    if (arrayResultingType.getLengthType().getMin().getText().equals(arrayResultingType.getLengthType().getMax().getText())) {
                        final String varName = varNameCreator.create("array");
                        pointers.add(varName);
                        sb.append(indentation)
                                .append("struct Array *")
                                .append(varName)
                                .append(" = ");

                        if (exp.getArray() instanceof ReferenceExpression refExp) {
                            sb.append("((")
                                    .append(cType(resultingType))
                                    .append(" *) ")
                                    .append(refExp.getReference().getText())
                                    .append("->values) + (");
                            dumpExpression(exp.getIndex(), sb, indentation, varNameCreator, pointers, null, 0);
                            sb.append(")");
                        }
                        else {
                            sb.append(PLACEHOLDER);
                        }
                        sb.append(";\n");

                        sb.append(indentation)
                                .append("memcpy(");
                        if (outOffset > 0) {
                            sb.append("((char *) ")
                                    .append(OUT_RESULT)
                                    .append(") + ")
                                    .append(outOffset);
                        }
                        else {
                            sb.append(OUT_RESULT);
                        }

                        sb.append(", ")
                                .append(varName)
                                .append("->values, ")
                                .append(varName)
                                .append("->length);\n");
                    }
                    else {
                        sb.append(PLACEHOLDER);
                    }
                }
                else {
                    sb.append(PLACEHOLDER);
                }
            }
            else {
                sb.append(PLACEHOLDER);
            }
        }
        else if (expression instanceof BooleanLiteralExpression exp) {
            sb.append(exp.getValue()? "1" : "0");
        }
        else if (expression instanceof ComplexExpression exp) {
            final String newIndentation;
            if (outVarName == null) {
                sb.append("{\n");
                newIndentation = indentation + INDENTATION;
            }
            else {
                newIndentation = indentation;
            }

            for (Statement statement : exp.getStatements()) {
                dumpStatement(statement, sb, newIndentation, varNameCreator, pointers);
            }
            sb.append(newIndentation);
            dumpExpression(exp.getExpression(), sb, newIndentation, varNameCreator, pointers, null, 0);

            if (outVarName == null) {
                sb.append("\n")
                        .append(indentation)
                        .append("}");
            }
        }
        else if (expression instanceof DifferentFromExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") != (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof DivisionExpression exp) {
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(" / ");
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
        }
        else if (expression instanceof EqualThanExpression exp) {
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(" == ");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            dumpExpression(exp.getFunction(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append("(");

            boolean separatorRequired = false;
            for (Expression parameter : exp.getParameters()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                dumpExpression(parameter, sb, indentation, varNameCreator, pointers, null, 0);
                separatorRequired = true;
            }
            sb.append(")");
        }
        else if (expression instanceof FunctionExpression exp) {
            // TODO: This must be adapted to C
            sb.append("(");
            boolean separatorRequired = false;
            for (FunctionParameter parameter : exp.getParameters()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                sb.append(cType(parameter.getType()))
                        .append(" ")
                        .append(parameter.getName().getText());
                separatorRequired = true;
            }

            sb.append(") -> ");
            dumpExpression(exp.getBody(), sb, indentation, varNameCreator, pointers, null, 0);
        }
        else if (expression instanceof GreaterOrEqualThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") >= (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof GreaterThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") > (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof IfExpression exp) {
            sb.append("if (");
            dumpExpression(exp.getCondition(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") ");
            if (exp.getThenClause() instanceof IfExpression) {
                final String newIndentation = indentation + INDENTATION;
                sb.append("\n")
                        .append(newIndentation);
                dumpExpression(exp.getThenClause(), sb, newIndentation, varNameCreator, pointers, null, 0);
            }
            else {
                dumpExpression(exp.getThenClause(), sb, indentation, varNameCreator, pointers, null, 0);
            }

            sb.append("\n")
                    .append(indentation);
            if (exp.getElseClause() instanceof IfExpression) {
                final String newIndentation = indentation + INDENTATION;
                sb.append("else\n")
                        .append(newIndentation);
                dumpExpression(exp.getElseClause(), sb, newIndentation, varNameCreator, pointers, null, 0);
            }
            else {
                sb.append("else ");
                dumpExpression(exp.getElseClause(), sb, indentation, varNameCreator, pointers, null, 0);
            }
        }
        else if (expression instanceof IntegerLiteralExpression exp) {
            sb.append(exp.getLiteral().getText());
        }
        else if (expression instanceof LowerOrEqualThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") <= (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof LowerThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") < (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof ModuleExpression exp) {
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(" % ");
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
        }
        else if (expression instanceof MultiplicationExpression exp) {
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(" * ");
            dumpExpressionWrappingIfRequired(this::areParenthesesRequiredOnMultiplication, exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
        }
        else if (expression instanceof OrExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") || (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else if (expression instanceof ReferenceExpression exp) {
            sb.append(exp.getReference().getText());
        }
        else if (expression instanceof RegisterConstructor exp) {
            if (outVarName != null) {
                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof ConstantDefinitionStatement constDef) {
                        if (constDef.getExpression() instanceof ReferenceExpression refExp && refExp.resultingType(ImmutableHashMap.empty(), logger) instanceof ArrayType arrayType) {
                            sb.append(indentation)
                                    .append(outVarName)
                                    .append("->")
                                    .append(constDef.getName().getText())
                                    .append(".length = ")
                                    .append(refExp.getReference().getText())
                                    .append("->length;\n");
                            sb.append(indentation)
                                    .append("memcpy(")
                                    .append(outVarName)
                                    .append("->")
                                    .append(constDef.getName().getText())
                                    .append(".values, ")
                                    .append(refExp.getReference().getText())
                                    .append("->values, ")
                                    .append(refExp.getReference().getText())
                                    .append("->length * sizeof(")
                                    .append(cType(arrayType.getItemType()))
                                    .append("));\n");
                        }
                        else {
                            sb.append(indentation)
                                    .append(outVarName)
                                    .append("->")
                                    .append(constDef.getName().getText())
                                    .append(" = ");
                            dumpExpression(constDef.getExpression(), sb, indentation, varNameCreator, pointers, null, 0);
                            sb.append(";\n");
                        }
                    }
                }
            }
            else {
                sb.append(exp.getRegisterName().getText());
                sb.append(" {\n");
                final String newIndentation = indentation + INDENTATION;
                for (Statement statement : exp.getStatements()) {
                    sb.append(newIndentation);
                    dumpStatement(statement, sb, newIndentation, varNameCreator, pointers);
                }
                sb.append(indentation)
                        .append("}");
            }
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            dumpExpression(exp.getRegister(), sb, indentation, varNameCreator, pointers, null, 0);
            if (exp.getRegister() instanceof ReferenceExpression refExp && pointers.contains(refExp.getReference().getText())) {
                sb.append("->");
            }
            else {
                sb.append(".");
            }

            sb.append(exp.getFieldName().getText());
        }
        else if (expression instanceof StringLiteralExpression exp) {
            if (outVarName != null) {
                final String text = exp.getLiteral().getText();
                final int textLength = text.length();
                for (int i = 1; i < textLength - 1; i++) {
                    sb.append(indentation)
                            .append(outVarName)
                            .append("[")
                            .append(outOffset + i - 1)
                            .append("] = '")
                            .append(text.charAt(i))
                            .append("';\n");
                }
            }
            else {
                sb.append(exp.getLiteral().getText());
            }
        }
        else if (expression instanceof SubtractionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(") - (");
            dumpExpression(exp.getRightExpression(), sb, indentation, varNameCreator, pointers, null, 0);
            sb.append(")");
        }
        else {
            throw new UnsupportedOperationException("Unknown expression type " + expression.getClass().getName());
        }
    }

    public void dumpConstantDefinition(String constantName, Expression expression, StringBuilder sb, String indentation, VariableNameCreator varNameCreator, MutableSet<String> pointers) {
        final Type constantType = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        if (expression instanceof ArrayConstructor arrayConstructionExp) {
            final ImmutableList<Expression> values = arrayConstructionExp.getValues();
            final int arrayLength = values.size();
            final Type itemType = ((ArrayType) constantType).getItemType();

            final String valuesVarName = varNameCreator.create(constantName + "Values");
            sb.append(indentation)
                    .append(cType(itemType))
                    .append(" ")
                    .append(valuesVarName)
                    .append("[")
                    .append(arrayLength)
                    .append("];\n");
            for (int i = 0; i < arrayLength; i++) {
                if (values.valueAt(i) instanceof StringLiteralExpression strLiteralExp) {
                    final String strVarName = varNameCreator.create("charValues");
                    final String strLength = strLiteralExp.resultingType(ImmutableHashMap.empty(), msg -> {}).getLengthType().getMax().getText();
                    sb.append(indentation)
                            .append("char ")
                            .append(strVarName)
                            .append("[")
                            .append(strLength)
                            .append("];\n");
                    dumpExpression(strLiteralExp, sb, indentation, varNameCreator, pointers, strVarName, 0);
                    sb.append(indentation)
                            .append(valuesVarName)
                            .append("[")
                            .append(i)
                            .append("].length = ")
                            .append(strLength)
                            .append(";\n");
                    sb.append(indentation)
                            .append(valuesVarName)
                            .append("[")
                            .append(i)
                            .append("].values = ")
                            .append(strVarName)
                            .append(";\n");
                }
            }

            sb.append(indentation)
                    .append("struct Array ")
                    .append(constantName)
                    .append(";\n");
            sb.append(indentation)
                    .append(constantName)
                    .append(".length = ")
                    .append(arrayLength)
                    .append(";\n");
            sb.append(indentation)
                    .append(constantName)
                    .append(".values = ")
                    .append(valuesVarName)
                    .append(";\n");
        }
        else if (expression instanceof FunctionExpression) {
            // Nothing to be done
        }
        else {
            sb.append(PLACEHOLDER);
        }
    }

    public void dumpStatement(Statement statement, StringBuilder sb, String indentation, VariableNameCreator varNameCreator, MutableSet<String> pointers) {
        if (statement instanceof TypeAliasStatement typeAlias) {
            sb.append("typedef ")
                    .append(cType(typeAlias.getType()))
                    .append(" ")
                    .append(typeAlias.getName().getText())
                    .append(";\n");
        }
        else if (statement instanceof ConstantDefinitionStatement constant) {
            dumpConstantDefinition(constant.getName().getText(), constant.getExpression(), sb, indentation, varNameCreator, pointers);
        }
        else {
            throw new UnsupportedOperationException("Unknown statement type " + statement.getClass().getName());
        }
    }

    private void traverseExpression(
            Expression expression,
            ImmutableList<String> spaceName,
            ImmutableList.Builder<CStruct> cStructsBuilder,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            MutableMap<RegisterType, String> registerNames,
            MutableSet<String> dependencies) {
        if (expression instanceof AdditionExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof AndExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof ArrayConcatenationExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            traverseExpression(exp.getArray(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getIndex(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof ComplexExpression exp) {
            final MutableMap<String, Type> scopeDefinitions = MutableHashMap.empty();
            final MutableSet<String> scopeDependencies = MutableHashSet.empty();
            for (Statement statement : exp.getStatements()) {
                traverseStatement(statement, spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, scopeDefinitions, scopeDependencies);
            }
            traverseExpression(exp.getExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, scopeDependencies);
            dependencies.addAll(scopeDependencies.filterNot(scopeDefinitions::containsKey));
        }
        else if (expression instanceof DivisionExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof EqualThanExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            for (Expression param : exp.getParameters()) {
                traverseExpression(param, spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            }
            traverseExpression(exp.getFunction(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof GreaterOrEqualThanExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof IfExpression exp) {
            traverseExpression(exp.getCondition(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getThenClause(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getElseClause(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof IntegerLiteralExpression) {
            // Nothing to track
        }
        else if (expression instanceof LowerThanExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof ModuleExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof MultiplicationExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof OrExpression exp) {
            traverseExpression(exp.getLeftExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
            traverseExpression(exp.getRightExpression(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof ReferenceExpression exp) {
            dependencies.add(exp.getReference().getText());
        }
        else if (expression instanceof RegisterConstructor exp) {
            final MutableMap<String, Type> scopeDefinitions = MutableHashMap.empty();
            final MutableSet<String> scopeDependencies = MutableHashSet.empty();
            for (Statement statement : exp.getStatements()) {
                traverseStatement(statement, spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, scopeDefinitions, scopeDependencies);
            }
            dependencies.addAll(scopeDependencies.filterNot(scopeDefinitions::containsKey));
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            traverseExpression(exp.getRegister(), spaceName, cStructsBuilder, cFunctionsBuilder, registerNames, dependencies);
        }
        else if (expression instanceof StringLiteralExpression) {
            // Nothing to track
        }
        else {
            throw new UnsupportedOperationException("traverseExpression unimplemented for expression " + expression.getClass().getSimpleName());
        }
    }

    private void traverseStatement(
            Statement statement,
            ImmutableList<String> spaceName,
            ImmutableList.Builder<CStruct> cStructsBuilder,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            MutableMap<RegisterType, String> registerNames,
            MutableMap<String, Type> definitions,
            MutableSet<String> dependencies) {
        if (statement instanceof TypeAliasStatement typeDef && typeDef.getType() instanceof RegisterType regType) {
            registerNames.put(regType, typeDef.getName().getText());
            cStructsBuilder.append(new CStruct(typeDef.getName().getText(), regType.getFields().entries().map(entry -> new CVariable(entry.key().getText(), cType(entry.value())))));
        }
        else if (statement instanceof ConstantDefinitionStatement constantDef) {
            if (constantDef.getExpression() instanceof FunctionExpression funcExpression) {
                final boolean isPublic = spaceName.isEmpty();
                final MutableMap<String, Type> paramTypes = MutableHashMap.empty();
                for (FunctionParameter funcParam : funcExpression.getParameters()) {
                    paramTypes.put(funcParam.getName().getText(), funcParam.getType());
                }

                final Type resultType = funcExpression.getBody().resultingType(paramTypes, msg -> {});
                final ImmutableList<String> innerSpaceName = spaceName.append(constantDef.getName().getText());
                final MutableSet<String> functionDependencies = MutableHashSet.empty();
                traverseExpression(funcExpression.getBody(), innerSpaceName, cStructsBuilder, cFunctionsBuilder, registerNames, functionDependencies);
                final ImmutableSet<String> paramNames = funcExpression.getParameters().map(p -> p.getName().getText()).toSet().toImmutable();
                MutableSetExtensions.retainNot(functionDependencies, paramNames::contains);

                final String functionName = innerSpaceName.reduce((a, b) -> a + "_" + b);
                final ImmutableList.Builder<CVariable> paramsBuilder = new ImmutableList.Builder<>();
                boolean resultAsParameter = false;
                if (resultType instanceof ArrayType arrayType) {
                    final String lengthMinText = arrayType.getLengthType().getMin().getText();
                    final String lengthMaxText = arrayType.getLengthType().getMax().getText();
                    final String cResultType = (lengthMinText.equals(lengthMaxText)?
                            cType(arrayType.getItemType()) :
                            cType(resultType)) + "*";
                    paramsBuilder.append(new CVariable(OUT_RESULT, cResultType));
                    resultAsParameter = true;
                }
                else if (resultType instanceof RegisterType) {
                    paramsBuilder.append(new CVariable(OUT_RESULT, cType(resultType) + " *"));
                    resultAsParameter = true;
                }

                for (String dependency : functionDependencies) {
                    final Type dependencyType = definitions.get(dependency);
                    final String cParamType;
                    if (dependencyType instanceof ArrayType arrayType) {
                        final String lengthMinText = arrayType.getLengthType().getMin().getText();
                        final String lengthMaxText = arrayType.getLengthType().getMax().getText();
                        cParamType = (lengthMinText.equals(lengthMaxText)?
                                cType(arrayType.getItemType()) :
                                cType(dependencyType)) + "*";
                    }
                    else {
                        cParamType = cType(dependencyType);
                    }

                    paramsBuilder.append(new CVariable(dependency, cParamType));
                }

                for (FunctionParameter param : funcExpression.getParameters()) {
                    final Type paramType = param.getType();
                    final String cType = cType(paramType);
                    final String ptrType = (paramType instanceof ArrayType || paramType instanceof RegisterType)? cType + " *" : cType;
                    paramsBuilder.append(new CVariable(param.getName().getText(), ptrType));
                }

                final String resultCType = resultAsParameter? "void" : cType(resultType);
                cFunctionsBuilder.append(new CFunction(functionName, paramsBuilder.build(), resultCType, funcExpression.getBody(), !isPublic, resultAsParameter));
                dependencies.addAll(functionDependencies);
            }

            definitions.put(constantDef.getName().getText(), constantDef.getExpression().resultingType(ImmutableHashMap.empty(), msg -> {}));
        }
    }

    public void generate(ImmutableList<Statement> statements, String path) {
        final ImmutableList.Builder<CStruct> cStructsBuilder = new ImmutableList.Builder<>();
        final ImmutableList.Builder<CFunction> cFunctionsBuilder = new ImmutableList.Builder<>();
        MutableMap<RegisterType, String> registerNames = MutableHashMap.empty();

        for (Statement statement : statements) {
            traverseStatement(statement, ImmutableList.empty(), cStructsBuilder, cFunctionsBuilder, registerNames, MutableHashMap.empty(), MutableHashSet.empty());
        }

        final ImmutableList<CStruct> cStructs = cStructsBuilder.build();
        final ImmutableList<CFunction> cFunctions = cFunctionsBuilder.build();

        final int lastSlash = path.lastIndexOf('/');
        final String filename = (lastSlash >= 0)? path.substring(lastSlash + 1) : path;

        try (FileOutputStream outStream = new FileOutputStream(path + ".h")) {
            final PrintWriter out = new PrintWriter(outStream, true);
            final String headerDefine = "_" + filename.toUpperCase() + "_H_";
            out.println("#ifndef " + headerDefine);
            out.println("#define " + headerDefine);
            out.println();
            out.println("struct Array {");
            out.println(INDENTATION + "int length;");
            out.println(INDENTATION + "void *values;");
            out.println("};");
            for (CStruct cStruct : cStructs) {
                out.println();
                out.println("struct " + cStruct.name + " {");
                for (CVariable variable : cStruct.fields) {
                    out.println(INDENTATION + variable.type + " " + variable.name + ";");
                }
                out.println("};");
                out.println();
            }

            for (CFunction cFunction : cFunctions) {
                if (!cFunction.isStatic) {
                    out.println(cFunction.returnType + " " + cFunction.name + "(" + cFunction.params.map(param -> param.type + " " + param.name).reduce((a, b) -> a + ", " + b, "") + ");");
                }
            }
            out.println("#endif /* " + headerDefine + " */");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream outStream = new FileOutputStream(path + ".c")) {
            final PrintWriter out = new PrintWriter(outStream, true);
            out.println("#include \"" + filename + ".h\"");
            out.println("#include <string.h>\n"); // Required for memcpy
            out.println();

            for (CFunction cFunction : cFunctions) {
                if (cFunction.isStatic) {
                    out.print("static ");
                }
                out.println(cFunction.returnType + " " + cFunction.name + "(" + cFunction.params.map(param -> param.type + " " + param.name).reduce((a, b) -> a + ", " + b, "") + ") {");
                final MutableSet<String> pointers = MutableHashSet.empty();
                for (CVariable param : cFunction.params) {
                    if (param.type.endsWith("*")) {
                        pointers.add(param.name);
                    }
                }

                final StringBuilder sb = new StringBuilder();
                dumpExpression(cFunction.body, sb, INDENTATION, new DefaultVariableNameCreator(), pointers, OUT_RESULT, 0);
                out.println(sb);
                out.println("}");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
