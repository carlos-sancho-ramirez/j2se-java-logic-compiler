package sword.logic.compiler.generator.c;

import sword.logic.expressions.Expression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

final class KeyPair {
    private final Expression mExpression;
    private final PersistenceChecker.InPersistence mInPersistence;

    KeyPair(Expression expression, PersistenceChecker.InPersistence inPersistence) {
        ensureNonNull(expression, inPersistence);
        mExpression = expression;
        mInPersistence = inPersistence;
    }

    @Override
    public int hashCode() {
        return mExpression.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof KeyPair that &&
                mExpression == that.mExpression &&
                mInPersistence.equals(that.mInPersistence);
    }
}
