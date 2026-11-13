package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaGreaterOrEqualThanExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaGreaterOrEqualThanExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText() {
        return mLeft.getText() + " >= " + mRight.getText();
    }
}
