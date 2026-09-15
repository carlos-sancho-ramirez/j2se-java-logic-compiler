package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class COrExpression implements CExpression {
    private final CExpression mLeft;
    private final CExpression mRight;

    public COrExpression(CExpression left, CExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText() {
        return mLeft.getText() + " || " + mRight.getText();
    }
}
