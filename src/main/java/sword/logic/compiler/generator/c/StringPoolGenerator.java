package sword.logic.compiler.generator.c;

import sword.collections.ImmutableIntValueMap;
import sword.collections.ImmutableList;
import sword.collections.MutableIntValueHashMap;
import sword.collections.MutableIntValueMap;
import sword.logic.expressions.ArrayConstructionExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
import sword.logic.expressions.EnumValueLiteralExpression;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.expressions.FunctionExecutionExpression;
import sword.logic.expressions.IfExpression;
import sword.logic.expressions.IntegerLiteralExpression;
import sword.logic.expressions.LeftRightExpression;
import sword.logic.expressions.ReferenceExpression;
import sword.logic.expressions.RegisterConstructionExpression;
import sword.logic.expressions.RegisterFieldAccessExpression;
import sword.logic.expressions.StringLiteralExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

final class StringPoolGenerator {

    static boolean canBeOptimizedInStringPool(ArrayConstructionExpression arrayExp) {
        final ImmutableList<Expression> arrayExpValues = arrayExp.getValues();
        if (!arrayExpValues.isEmpty() && arrayExpValues.allMatch(v -> v instanceof StringLiteralExpression)) {
            final int valueLength = ((StringLiteralExpression) arrayExpValues.valueAt(0)).getArrayLength();
            return arrayExpValues.allMatch(v -> ((StringLiteralExpression) v).getArrayLength() == valueLength);
        }

        return false;
    }

    private String findStringLiteralsInExpression(Expression expression, String stringPool, MutableIntValueMap<StringLiteralExpression> stringPoolIndexes) {
        if (expression instanceof ArrayConstructionExpression exp) {
            if (canBeOptimizedInStringPool(exp)) {
                for (Expression value : exp.getValues()) {
                    final StringLiteralExpression strValue = (StringLiteralExpression) value;
                    stringPoolIndexes.put(strValue, stringPool.length());
                    stringPool += strValue.getLiteral().substring(1, strValue.getLiteral().length() - 1);
                }
            }
            else {
                for (Expression value : exp.getValues()) {
                    stringPool = findStringLiteralsInExpression(value, stringPool, stringPoolIndexes);
                }
            }

            return stringPool;
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            stringPool = findStringLiteralsInExpression(exp.getArray(), stringPool, stringPoolIndexes);
            return findStringLiteralsInExpression(exp.getIndex(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof ComplexExpression exp) {
            for (Statement statement : exp.getStatements()) {
                stringPool = findStringLiteralsInStatement(statement, stringPool, stringPoolIndexes);
            }

            return findStringLiteralsInExpression(exp.getExpression(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof EnumValueLiteralExpression) {
            return stringPool;
        }
        else if (expression instanceof FunctionDefinitionExpression exp) {
            return findStringLiteralsInExpression(exp.getBody(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            for (Expression parameter : exp.getParameters()) {
                stringPool = findStringLiteralsInExpression(parameter, stringPool, stringPoolIndexes);
            }

            return findStringLiteralsInExpression(exp.getFunction(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof IfExpression exp) {
            stringPool = findStringLiteralsInExpression(exp.getCondition(), stringPool, stringPoolIndexes);
            stringPool = findStringLiteralsInExpression(exp.getThenClause(), stringPool, stringPoolIndexes);
            return findStringLiteralsInExpression(exp.getElseClause(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof IntegerLiteralExpression) {
            return stringPool;
        }
        else if (expression instanceof LeftRightExpression exp) {
            stringPool = findStringLiteralsInExpression(exp.getLeftExpression(), stringPool, stringPoolIndexes);
            return findStringLiteralsInExpression(exp.getRightExpression(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof ReferenceExpression) {
            return stringPool;
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            for (Statement statement : exp.getStatements()) {
                stringPool = findStringLiteralsInStatement(statement, stringPool, stringPoolIndexes);
            }

            return stringPool;
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            return findStringLiteralsInExpression(exp.getRegister(), stringPool, stringPoolIndexes);
        }
        else if (expression instanceof StringLiteralExpression exp) {
            final String unquoted = exp.getLiteral().substring(1, exp.getLiteral().length() - 1);
            final int index = stringPool.indexOf(unquoted);
            if (index >= 0) {
                stringPoolIndexes.put(exp, index);
                return stringPool;
            }
            else {
                final int unquotedLength = unquoted.length();
                for (int i = unquotedLength - 1; i > 0; i--) {
                    if (stringPool.endsWith(unquoted.substring(0, i))) {
                        stringPoolIndexes.put(exp, stringPool.length() - (unquotedLength - i));
                        return stringPool + unquoted.substring(i);
                    }
                }

                stringPoolIndexes.put(exp, stringPool.length());
                return stringPool + unquoted;
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private String findStringLiteralsInStatement(Statement statement, String stringPool, MutableIntValueMap<StringLiteralExpression> stringPoolIndexes) {
        return (statement instanceof ConstantDefinitionStatement constDef)?
                findStringLiteralsInExpression(constDef.getExpression(), stringPool, stringPoolIndexes) : stringPool;
    }

    Result generate(ImmutableList<Statement> statements) {
        String stringPool = "";
        final MutableIntValueMap<StringLiteralExpression> stringPoolIndexes = MutableIntValueHashMap.empty();
        for (Statement statement : statements) {
            stringPool = findStringLiteralsInStatement(statement, stringPool, stringPoolIndexes);
        }

        return new Result(stringPool, stringPoolIndexes.toImmutable());
    }

    static final class Result {
        private final String mPool;
        private final ImmutableIntValueMap<StringLiteralExpression> mIndexes;

        Result(String pool, ImmutableIntValueMap<StringLiteralExpression> indexes) {
            ensureNonNull(pool, indexes);
            mPool = pool;
            mIndexes = indexes;
        }

        String getPool() {
            return mPool;
        }

        ImmutableIntValueMap<StringLiteralExpression> getIndexes() {
            return mIndexes;
        }
    }
}
