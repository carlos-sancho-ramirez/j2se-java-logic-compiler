package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.generator.java.expressions.JavaMultiplicationExpression.composeText;

public final class JavaModuleExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaModuleExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return composeText(importedClassReferences, mLeft, mRight, "%");
    }
}
