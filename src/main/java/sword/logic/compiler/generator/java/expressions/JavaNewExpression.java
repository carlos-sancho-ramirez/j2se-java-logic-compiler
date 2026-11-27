package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaClassReference;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaNewExpression implements JavaExpression {
    private final JavaClassReference mClassReference;
    private final ImmutableList<JavaExpression> mParameters;

    public JavaNewExpression(
            JavaClassReference classReference,
            ImmutableList<JavaExpression> parameters) {
        ensureNonNull(classReference, parameters);
        mClassReference = classReference;
        mParameters = parameters;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "new " + mClassReference.getTypeText(importedClassReferences) + "(" + mParameters
                .map(p -> p.getText(importedClassReferences))
                .reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
