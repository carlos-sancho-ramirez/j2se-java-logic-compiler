package sword.logic.compiler;

import sword.collections.ImmutableMap;

public final class StatementDumper {
    private static final String INDENTATION = "  ";

    private void dumpType(Type type, StringBuilder sb, String indentation) {
        if (type instanceof ArrayType arrayType) {
            sb.append("Array[");
            dumpType(arrayType.getItemType(), sb, indentation);
            sb.append("]");
        }
        else if (type instanceof IntegerType intType) {
            sb.append("Int[")
                    .append(intType.getMin().getText())
                    .append("..")
                    .append(intType.getMax().getText())
                    .append("]");
        }
        else if (type instanceof RegisterType registerType) {
            sb.append("{\n");
            for (ImmutableMap.Entry<Token, Type> fieldEntry : registerType.getFields().entries()) {
                sb.append(indentation)
                        .append(INDENTATION)
                        .append(fieldEntry.key().getText())
                        .append(": ");
                dumpType(fieldEntry.value(), sb, indentation + INDENTATION);
                sb.append(";\n");
            }

            sb.append(indentation)
                    .append("}");
        }
        else if (type instanceof SimpleType simpleType) {
            sb.append(simpleType.getName().getText());
        }
        else {
            throw new UnsupportedOperationException("Unknown type " + type.getClass().getName());
        }
    }

    public void dumpExpression(Expression expression, StringBuilder sb, String indentation) {
        if (expression instanceof AdditionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") + (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof AndExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") & (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof ArrayConcatenationExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") + (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof ArrayConstructor exp) {
            sb.append("Array (");
            boolean separatorRequired = false;
            for (Expression value : exp.getValues()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                dumpExpression(value, sb, indentation);
                separatorRequired = true;
            }
            sb.append(")");
        }
        else if (expression instanceof ArrayLengthExpression exp) {
            sb.append("(");
            dumpExpression(exp.getArray(), sb, indentation);
            sb.append(").length");
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            sb.append("(");
            dumpExpression(exp.getArray(), sb, indentation);
            sb.append(")[");
            dumpExpression(exp.getIndex(), sb, indentation);
            sb.append("]");
        }
        else if (expression instanceof BooleanLiteralExpression exp) {
            sb.append(exp.getValue()? "TRUE" : "FALSE");
        }
        else if (expression instanceof ComplexExpression exp) {
            sb.append("{\n");
            final String newIndentation = indentation + INDENTATION;
            for (Statement statement : exp.getStatements()) {
                sb.append(newIndentation);
                dump(statement, sb, newIndentation);
            }
            sb.append(newIndentation);
            dumpExpression(exp.getExpression(), sb, newIndentation);
            sb.append("\n")
                    .append(indentation)
                    .append("}");
        }
        else if (expression instanceof DifferentFromExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") != (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof DivisionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") / (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof EqualThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") == (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getFunction(), sb, indentation);
            sb.append(")(");

            boolean separatorRequired = false;
            for (Expression parameter : exp.getParameters()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                dumpExpression(parameter, sb, indentation);
                separatorRequired = true;
            }
            sb.append(")");
        }
        else if (expression instanceof FunctionExpression exp) {
            sb.append("(");
            boolean separatorRequired = false;
            for (FunctionParameter parameter : exp.getParameters()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                sb.append(parameter.getName().getText())
                        .append(": ");
                dumpType(parameter.getType(), sb, indentation);
                separatorRequired = true;
            }

            sb.append(") -> ");
            dumpExpression(exp.getBody(), sb, indentation);
        }
        else if (expression instanceof GreaterOrEqualThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") >= (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof GreaterThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") > (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof IfExpression exp) {
            sb.append("if ");
            dumpExpression(exp.getCondition(), sb, indentation);
            sb.append("\n")
                    .append(indentation);
            if (exp.getThenClause() instanceof IfExpression) {
                final String newIndentation = indentation + INDENTATION;
                sb.append("then\n")
                        .append(newIndentation);
                dumpExpression(exp.getThenClause(), sb, newIndentation);
            }
            else {
                sb.append("then ");
                dumpExpression(exp.getThenClause(), sb, indentation);
            }

            sb.append("\n")
                    .append(indentation);
            if (exp.getElseClause() instanceof IfExpression) {
                final String newIndentation = indentation + INDENTATION;
                sb.append("else\n")
                        .append(newIndentation);
                dumpExpression(exp.getElseClause(), sb, newIndentation);
            }
            else {
                sb.append("else ");
                dumpExpression(exp.getElseClause(), sb, indentation);
            }
        }
        else if (expression instanceof IntegerLiteralExpression exp) {
            sb.append(exp.getLiteral().getText());
        }
        else if (expression instanceof LowerOrEqualThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") <= (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof LowerThanExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") < (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof ModuleExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") % (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof MultiplicationExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") * (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof OrExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") | (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else if (expression instanceof ReferenceExpression exp) {
            sb.append(exp.getReference().getText());
        }
        else if (expression instanceof RegisterConstructor exp) {
            sb.append(exp.getType().getText());
            sb.append(" {\n");
            final String newIndentation = indentation + INDENTATION;
            for (Statement statement : exp.getStatements()) {
                sb.append(newIndentation);
                dump(statement, sb, newIndentation);
            }
            sb.append(indentation)
                    .append("}");
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            sb.append("(");
            dumpExpression(exp.getRegister(), sb, indentation);
            sb.append(").")
                    .append(exp.getFieldName().getText());
        }
        else if (expression instanceof StringLiteralExpression exp) {
            sb.append(exp.getLiteral().getText());
        }
        else if (expression instanceof SubtractionExpression exp) {
            sb.append("(");
            dumpExpression(exp.getLeftExpression(), sb, indentation);
            sb.append(") - (");
            dumpExpression(exp.getRightExpression(), sb, indentation);
            sb.append(")");
        }
        else {
            throw new UnsupportedOperationException("Unknown expression type " + expression.getClass().getName());
        }
    }

    public void dump(Statement statement, StringBuilder sb, String indentation) {
        if (statement instanceof TypeAliasStatement typeAlias) {
            sb.append("type " + typeAlias.getName().getText() + " = ");
            dumpType(typeAlias.getType(), sb, indentation);
            sb.append(";\n");
        }
        else if (statement instanceof ConstantDefinitionStatement constant) {
            sb.append(constant.getName().getText()).append(" = ");
            dumpExpression(constant.getExpression(), sb, indentation);
            sb.append(";\n");
        }
        else {
            throw new UnsupportedOperationException("Unknown statement type " + statement.getClass().getName());
        }
    }
}
