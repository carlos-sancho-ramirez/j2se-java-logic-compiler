package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ModuleExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public ModuleExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }
}
