package sword.logic.compiler.generator.java.entities;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaWildcardClassReference implements JavaWildcardableClassReference {
    private final JavaPackage mPackage;

    public JavaWildcardClassReference(JavaPackage javaPackage) {
        ensureNonNull(javaPackage);
        mPackage = javaPackage;
    }

    public JavaPackage getPackage() {
        return mPackage;
    }

    @Override
    public int hashCode() {
        return mPackage.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaWildcardClassReference that &&
                mPackage.equals(that.mPackage);
    }
}
