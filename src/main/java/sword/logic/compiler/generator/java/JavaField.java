package sword.logic.compiler.generator.java;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaField {
    public static final class Modifiers {
        public static final int PUBLIC = 1;
        public static final int PROTECTED = 2;
        public static final int PRIVATE = 4;
        public static final int STATIC = 8;
        public static final int FINAL = 16;

        private Modifiers() {
        }
    }

    private final JavaClass mClass;
    private final JavaVariable mVariable;
    private final int mModifiers;

    JavaField(JavaClass javaClass, JavaVariable variable, int modifiers) {
        ensureNonNull(javaClass, variable);
        mClass = javaClass;
        mVariable = variable;
        mModifiers = modifiers;
    }

    public String getName() {
        return mVariable.getName();
    }

    public JavaType getType() {
        return mVariable.getType();
    }

    public boolean isPublic() {
        return (mModifiers & Modifiers.PUBLIC) != 0;
    }

    public boolean isProtected() {
        return (mModifiers & Modifiers.PROTECTED) != 0;
    }

    public boolean isPrivate() {
        return (mModifiers & Modifiers.PRIVATE) != 0;
    }

    public boolean isStatic() {
        return (mModifiers & Modifiers.STATIC) != 0;
    }

    public boolean isFinal() {
        return (mModifiers & Modifiers.FINAL) != 0;
    }

    @Override
    public int hashCode() {
        return mVariable.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaField that &&
                mClass.equals(that.mClass) &&
                mVariable.equals(that.mVariable) &&
                mModifiers == that.mModifiers;
    }
}
