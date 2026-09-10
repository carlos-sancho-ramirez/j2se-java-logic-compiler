package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.generator.c.expressions.CMultiplicationExpression.requiresParentheses;

public final class CDivisionExpression implements CExpression {
    private final CExpression mLeft;
    private final CExpression mRight;

    public CDivisionExpression(CExpression left, CExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText() {
        final String leftText = mLeft.getText();
        final String rightText = mRight.getText();
        return (requiresParentheses(mLeft)? "(" + leftText + ")" : leftText) + " / " +
                (requiresParentheses(mRight)? "(" + rightText + ")" : rightText);
    }
}
