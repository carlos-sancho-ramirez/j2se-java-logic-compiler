package sword.logic.compiler.generator.java;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaClassReference implements JavaType {
    public static boolean isValidClassName(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        else {
            final int length = className.length();
            char ch = className.charAt(0);
            if (ch < 'A' || ch > 'Z') {
                return false;
            }
            else {
                for (int i = 1; i < length; i++) {
                    ch = className.charAt(i);
                    if ((ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') && (ch < '0' || ch > '9')) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private final JavaPackage mPackage;
    private final String mClassName;

    public JavaClassReference(JavaPackage javaPackage, String className) {
        ensureNonNull(javaPackage);
        ensureValidArguments(isValidClassName(className));
        mPackage = javaPackage;
        mClassName = className;
    }

    public JavaPackage getPackage() {
        return mPackage;
    }

    public String getName() {
        return mClassName;
    }

    public JavaClass newClass(int modifiers) {
        return new JavaClass(this, modifiers);
    }

    @Override
    public int hashCode() {
        return mClassName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaClassReference that &&
                mPackage.equals(that.mPackage) &&
                mClassName.equals(that.mClassName);
    }
}
