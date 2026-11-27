package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.types.JavaType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaGenericClassReference implements JavaType {
    private final JavaClassReference mRawReference;
    private final ImmutableList<JavaType> mTypes;

    public JavaGenericClassReference(JavaClassReference rawReference, ImmutableList<JavaType> types) {
        ensureNonNull(rawReference, types);
        mRawReference = rawReference;
        mTypes = types;
    }

    public JavaClassReference getRawReference() {
        return mRawReference;
    }

    public ImmutableList<JavaType> getTypes() {
        return mTypes;
    }

    @Override
    public int hashCode() {
        return mRawReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaGenericClassReference that &&
                mRawReference.equals(that.mRawReference) &&
                mTypes.equals(that.mTypes);
    }

    @Override
    public String getTypeText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mRawReference.getTypeText(importedClassReferences) + "<" + mTypes.map(t -> t.getTypeText(importedClassReferences)).reduce((a, b) -> a + ", " + b, "") + ">";
    }

    @Override
    public String getSignatureType() {
        return mRawReference.getSignatureType();
    }
}
