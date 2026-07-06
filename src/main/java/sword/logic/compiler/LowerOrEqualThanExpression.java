package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class LowerOrEqualThanExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public LowerOrEqualThanExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }
}
