package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaMultiplicationExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaMultiplicationExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    private static boolean requiresParentheses(JavaExpression exp) {
        return exp instanceof JavaAdditionExpression ||
                exp instanceof JavaSubtractionExpression;
    }

    static String composeText(JavaExpression left, JavaExpression right, String operator) {
        final String leftText = left.getText();
        final String rightText = right.getText();
        return (requiresParentheses(left)? "(" + leftText + ") " : leftText + " ") + operator +
                (requiresParentheses(right)? " (" + rightText + ")" : " " + rightText);
    }

    @Override
    public String getText() {
        return composeText(mLeft, mRight, "*");
    }
}
