package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.collections.MutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

public final class JavaEnum implements JavaClass {
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

    private final MutableList<JavaEnumValue> mAddedValues = MutableList.empty();

    JavaEnum(JavaClassReference reference, int modifiers) {
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

    public ImmutableList<JavaEnumValue> getValues() {
        return mAddedValues.toImmutable();
    }

    public JavaEnumValue addValue(String name) {
        ensureValidState(mAddedValues.allMatch(f -> !f.getName().equals(name)));
        final JavaEnumValue newEnumValue = new JavaEnumValue(this, name);
        mAddedValues.append(newEnumValue);
        return newEnumValue;
    }

    @Override
    public int hashCode() {
        return mReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaEnum that &&
                mReference.equals(that.mReference) &&
                mModifiers == that.mModifiers;
    }
}
