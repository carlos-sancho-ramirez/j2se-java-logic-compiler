package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayConcatenationExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaArrayConcatenationExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    private static boolean requiresParentheses(JavaExpression exp) {
        return exp instanceof JavaTernaryExpression;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        final String leftText = mLeft.getText(importedClassReferences);
        final String rightText = mRight.getText(importedClassReferences);
        return (requiresParentheses(mLeft)? "(" + leftText + ")" : leftText) + " + " +
                (requiresParentheses(mRight)? "(" + rightText + ")" : rightText);
    }
}
