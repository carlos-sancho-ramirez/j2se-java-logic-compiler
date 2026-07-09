package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class DivisionExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public DivisionExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }
}
