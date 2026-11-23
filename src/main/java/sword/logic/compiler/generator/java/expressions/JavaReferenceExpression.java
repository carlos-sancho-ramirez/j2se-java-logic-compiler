package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaReferenceExpression implements JavaAssignableExpression {
    private final String mReference;

    public JavaReferenceExpression(String reference) {
        ensureNonNull(reference);
        mReference = reference;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mReference;
    }
}
