package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.collections.MutableList;
import sword.logic.compiler.generator.java.statements.JavaStatement;
import sword.logic.compiler.generator.java.types.JavaType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

public final class JavaNormalClass implements JavaClass {
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
    private final MutableList<JavaConstructor> mAddedConstructors = MutableList.empty();
    private final MutableList<JavaMethod> mAddedMethods = MutableList.empty();

    JavaNormalClass(JavaClassReference reference, int modifiers) {
        ensureNonNull(reference);
        mReference = reference;
        mModifiers = modifiers;
    }

    @Override
    public JavaPackage getPackage() {
        return mReference.getPackage();
    }

    @Override
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

    public ImmutableList<JavaConstructor> getConstructors() {
        return mAddedConstructors.toImmutable();
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

    public JavaConstructor addConstructor(
            ImmutableList<JavaVariable> parameters,
            int modifiers,
            ImmutableList<JavaStatement> statements) {
        ensureValidState(mAddedConstructors.allMatch(c -> !c.getParameters().map(JavaVariable::getType).equals(parameters.map(JavaVariable::getType))));
        final JavaConstructor newConstructor = new JavaConstructor(this, parameters, modifiers, statements);
        mAddedConstructors.append(newConstructor);
        return newConstructor;
    }

    public JavaMethodReference newMethodReference(String methodName, ImmutableList<JavaType> parameters, JavaType returnType) {
        return new JavaMethodReference(this, methodName, parameters, returnType);
    }

    public JavaMethod addMethod(JavaMethodReference methodReference, ImmutableList<String> parameterNames, int modifiers, ImmutableList<JavaStatement> statements) {
        ensureValidState(methodReference.getJavaClass() == this);
        ensureValidState(mAddedMethods.allMatch(m -> !m.getReference().equals(methodReference)));
        final JavaMethod newMethod = new JavaMethod(methodReference, parameterNames, modifiers, statements);
        mAddedMethods.append(newMethod);
        return newMethod;
    }

    @Override
    public int hashCode() {
        return mReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaNormalClass that &&
                mReference.equals(that.mReference) &&
                mModifiers == that.mModifiers;
    }
}
