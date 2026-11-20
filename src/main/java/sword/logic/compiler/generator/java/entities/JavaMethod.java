package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.java.statements.JavaStatement;
import sword.logic.compiler.generator.java.types.JavaVoidableType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaMethod {
    public static final class Modifiers {
        public static final int PUBLIC = 1;
        public static final int PROTECTED = 2;
        public static final int PRIVATE = 4;
        public static final int STATIC = 8;
        public static final int FINAL = 16;

        private Modifiers() {
        }
    }

    private final JavaMethodReference mReference;
    private final ImmutableList<String> mParameterNames;
    private final int mModifiers;
    private final ImmutableList<JavaStatement> mStatements;

    public JavaMethod(
            JavaMethodReference reference,
            ImmutableList<String> parameterNames,
            int modifiers,
            ImmutableList<JavaStatement> statements) {
        ensureNonNull(reference, parameterNames, statements);
        ensureValidArguments(parameterNames.size() == reference.getParameters().size());
        mReference = reference;
        mParameterNames = parameterNames;
        mModifiers = modifiers;
        mStatements = statements;
    }

    public JavaMethodReference getReference() {
        return mReference;
    }

    public String getName() {
        return mReference.getName();
    }

    public ImmutableList<JavaVariable> getParameters() {
        return mParameterNames.indexes().map(index -> new JavaVariable(mParameterNames.valueAt(index), mReference.getParameters().valueAt(index)));
    }

    public JavaVoidableType getReturnType() {
        return mReference.getReturnType();
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

    public ImmutableList<JavaStatement> getStatements() {
        return mStatements;
    }

    @Override
    public int hashCode() {
        return mReference.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaMethod that &&
                mReference.equals(that.mReference) &&
                mParameterNames.equals(that.mParameterNames) &&
                mModifiers == that.mModifiers &&
                mStatements.equals(that.mStatements);
    }
}
