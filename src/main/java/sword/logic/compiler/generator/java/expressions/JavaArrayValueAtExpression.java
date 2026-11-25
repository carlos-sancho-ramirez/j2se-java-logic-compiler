package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayValueAtExpression implements JavaExpression {
    private final JavaExpression mArray;
    private final JavaExpression mIndex;

    public JavaArrayValueAtExpression(JavaExpression array, JavaExpression index) {
        ensureNonNull(array, index);
        mArray = array;
        mIndex = index;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mArray.getText(importedClassReferences) + ".valueAt(" + mIndex.getText(importedClassReferences) + ")";
    }
}
