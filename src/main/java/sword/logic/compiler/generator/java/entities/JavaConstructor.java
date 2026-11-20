package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.java.statements.JavaStatement;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaConstructor {
    public static final class Modifiers {
        public static final int PUBLIC = 1;
        public static final int PROTECTED = 2;
        public static final int PRIVATE = 4;

        private Modifiers() {
        }
    }

    private final JavaNormalClass mClass;
    private final ImmutableList<JavaVariable> mParameters;
    private final int mModifiers;
    private final ImmutableList<JavaStatement> mStatements;

    public JavaConstructor(
            JavaNormalClass javaClass,
            ImmutableList<JavaVariable> parameters,
            int modifiers,
            ImmutableList<JavaStatement> statements) {
        ensureNonNull(javaClass, parameters, statements);
        ensureValidArguments(parameters.map(JavaVariable::getName).toSet().size() == parameters.size());
        mClass = javaClass;
        mParameters = parameters;
        mModifiers = modifiers;
        mStatements = statements;
    }

    public JavaNormalClass getJavaClass() {
        return mClass;
    }

    public ImmutableList<JavaVariable> getParameters() {
        return mParameters;
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

    public ImmutableList<JavaStatement> getStatements() {
        return mStatements;
    }

    @Override
    public int hashCode() {
        return mParameters.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaConstructor that &&
                mClass.equals(that.mClass) &&
                mParameters.equals(that.mParameters) &&
                mModifiers == that.mModifiers &&
                mStatements.equals(that.mStatements);
    }
}
