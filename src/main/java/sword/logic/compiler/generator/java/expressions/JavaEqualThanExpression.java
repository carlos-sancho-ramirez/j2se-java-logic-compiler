package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaEqualThanExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaEqualThanExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mLeft.getText(importedClassReferences) + " == " + mRight.getText(importedClassReferences);
    }
}
