package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

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

    static String composeText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, JavaExpression left, JavaExpression right, String operator) {
        final String leftText = left.getText(importedClassReferences);
        final String rightText = right.getText(importedClassReferences);
        return (requiresParentheses(left)? "(" + leftText + ") " : leftText + " ") + operator +
                (requiresParentheses(right)? " (" + rightText + ")" : " " + rightText);
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return composeText(importedClassReferences, mLeft, mRight, "*");
    }
}
