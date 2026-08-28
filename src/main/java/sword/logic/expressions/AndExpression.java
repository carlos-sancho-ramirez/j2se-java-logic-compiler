package sword.logic.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class AndExpression implements LeftRightExpression {
    private final Expression mLeft;
    private final Expression mRight;

    public AndExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public Expression getLeftExpression() {
        return mLeft;
    }

    @Override
    public Expression getRightExpression() {
        return mRight;
    }
}
