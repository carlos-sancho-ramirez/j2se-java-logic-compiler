package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayLengthExpression implements JavaExpression {
    private final JavaExpression mArray;

    public JavaArrayLengthExpression(JavaExpression array) {
        ensureNonNull(array);
        mArray = array;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return mArray.getText(importedClassReferences) + ".length";
    }
}
