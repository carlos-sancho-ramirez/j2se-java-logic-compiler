package sword.logic.compiler;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.expressions.AdditionExpression;
import sword.logic.syntax_tree.expressions.AndExpression;
import sword.logic.syntax_tree.expressions.ArrayConcatenationExpression;
import sword.logic.syntax_tree.expressions.ArrayConstructor;
import sword.logic.syntax_tree.expressions.ArrayLengthExpression;
import sword.logic.syntax_tree.expressions.ArrayValueAtExpression;
import sword.logic.syntax_tree.expressions.BooleanLiteralExpression;
import sword.logic.syntax_tree.expressions.ComplexExpression;
import sword.logic.syntax_tree.expressions.DifferentFromExpression;
import sword.logic.syntax_tree.expressions.DivisionExpression;
import sword.logic.syntax_tree.expressions.EqualThanExpression;
import sword.logic.syntax_tree.expressions.Expression;
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
import sword.logic.syntax_tree.types.EnumType;
import sword.logic.syntax_tree.types.FunctionType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.RegisterType;
import sword.logic.syntax_tree.types.Type;

public final class StatementDumper {
    private static final String INDENTATION = "  ";

    private void dumpType(Type type, StringBuilder sb, String indentation) {
        if (type instanceof ArrayType arrayType) {
            sb.append("Array[length=");
            dumpType(arrayType.getLengthType(), sb, indentation);
            sb.append("; item=");
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
        else if (type instanceof EnumType enumType) {
            sb.append(enumType.getName().getText());
        }
        else if (type instanceof FunctionType funcType) {
            sb.append("(");
            boolean separatorRequired = false;
            for (Type paramType : funcType.getParameterTypes()) {
                if (separatorRequired) {
                    sb.append(", ");
                }

                dumpType(paramType, sb, indentation);
                separatorRequired = true;
            }
            sb.append(") -> ");
            dumpType(funcType.getResultType(), sb, indentation);
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
            sb.append(constant.getName().getText())
                    .append(": ");
            dumpType(constant.getExpression().resultingType(ImmutableHashMap.empty(), msg -> {}), sb, indentation);
            sb.append(" = ");
            dumpExpression(constant.getExpression(), sb, indentation);
            sb.append(";\n");
        }
        else {
            throw new UnsupportedOperationException("Unknown statement type " + statement.getClass().getName());
        }
    }
}
