package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CMultiplicationExpression implements CExpression {
    private final CExpression mLeft;
    private final CExpression mRight;

    public CMultiplicationExpression(CExpression left, CExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    static boolean requiresParentheses(CExpression exp) {
        return exp instanceof CAdditionExpression ||
                exp instanceof CSubtractionExpression;
    }

    @Override
    public String getText() {
        final String leftText = mLeft.getText();
        final String rightText = mRight.getText();
        return (requiresParentheses(mLeft)? "(" + leftText + ")" : leftText) + " * " +
                (requiresParentheses(mRight)? "(" + rightText + ")" : rightText);
    }
}
