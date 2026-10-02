package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CAndExpression implements CExpression {
    private final CExpression mLeft;
    private final CExpression mRight;

    public CAndExpression(CExpression left, CExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    private static boolean requiresParentheses(CExpression exp) {
        return exp instanceof COrExpression;
    }

    @Override
    public String getText() {
        final String leftText = mLeft.getText();
        final String rightText = mRight.getText();
        return (requiresParentheses(mLeft)? "(" + leftText + ")" : leftText) + " && " +
                (requiresParentheses(mRight)? "(" + rightText +")" : rightText);
    }
}
