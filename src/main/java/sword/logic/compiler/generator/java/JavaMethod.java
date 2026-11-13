package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.java.statements.JavaStatement;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.generator.java.JavaVariable.isValidVariableName;

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

    private final JavaClass mClass;
    private final String mName;
    private final ImmutableList<JavaVariable> mParameters;
    private final JavaVoidableType mReturnType;
    private final int mModifiers;
    private final ImmutableList<JavaStatement> mStatements;

    public JavaMethod(
            JavaClass javaClass,
            String methodName,
            ImmutableList<JavaVariable> parameters,
            JavaVoidableType returnType,
            int modifiers,
            ImmutableList<JavaStatement> statements) {
        ensureNonNull(javaClass, parameters, returnType, statements);
        ensureValidArguments(isValidVariableName(methodName));
        ensureValidArguments(!statements.isEmpty() || returnType == JavaVoidType.getInstance());
        // TODO: We should ensure that last statement is a return or a throw
        mClass = javaClass;
        mName = methodName;
        mParameters = parameters;
        mReturnType = returnType;
        mModifiers = modifiers;
        mStatements = statements;
    }

    public String getName() {
        return mName;
    }

    public ImmutableList<JavaVariable> getParameters() {
        return mParameters;
    }

    public JavaVoidableType getReturnType() {
        return mReturnType;
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
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaMethod that &&
                mClass.equals(that.mClass) &&
                mName.equals(that.mName) &&
                mParameters.equals(that.mParameters) &&
                mReturnType.equals(that.mReturnType) &&
                mModifiers == that.mModifiers;
    }
}
