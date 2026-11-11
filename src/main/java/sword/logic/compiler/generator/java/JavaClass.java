package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.collections.MutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

public final class JavaClass {
    public static final class Modifiers {
        public static final int PUBLIC = 1;
        public static final int PROTECTED = 2;
        public static final int PRIVATE = 4;
        public static final int STATIC = 8;
        public static final int FINAL = 16;

        private Modifiers() {
        }
    }

    private final JavaClassReference mReference;
    private final int mModifiers;

    private final MutableList<JavaField> mAddedFields = MutableList.empty();
    private final MutableList<JavaMethod> mAddedMethods = MutableList.empty();

    JavaClass(JavaClassReference reference, int modifiers) {
        ensureNonNull(reference);
        mReference = reference;
        mModifiers = modifiers;
    }

    public JavaPackage getPackage() {
        return mReference.getPackage();
    }

    public String getName() {
        return mReference.getName();
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

    public ImmutableList<JavaField> getFields() {
        return mAddedFields.toImmutable();
    }

    public ImmutableList<JavaMethod> getMethods() {
        return mAddedMethods.toImmutable();
    }

    public JavaField addField(String fieldName, JavaType type, int modifiers) {
        ensureValidState(mAddedFields.allMatch(f -> !f.getName().equals(fieldName)));
        final JavaField newField = new JavaField(this, new JavaVariable(fieldName, type), modifiers);
        mAddedFields.append(newField);
        return newField;
    }

    public JavaMethod addMethod(String methodName, ImmutableList<JavaVariable> parameters, JavaVoidableType returnType, int modifiers) {
        ensureValidState(mAddedMethods.allMatch(m -> !m.getName().equals(methodName)));
        final JavaMethod newMethod = new JavaMethod(this, methodName, parameters, returnType, modifiers);
        mAddedMethods.append(newMethod);
        return newMethod;
    }

    @Override
    public int hashCode() {
        return mReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaClass that &&
                mReference.equals(that.mReference) &&
                mModifiers == that.mModifiers;
    }
}
