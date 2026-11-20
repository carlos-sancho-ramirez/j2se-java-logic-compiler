package sword.logic.compiler.generator.java.entities;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.java.types.JavaType;
import sword.logic.compiler.generator.java.types.JavaVoidableType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.generator.java.entities.JavaVariable.isValidVariableName;

public final class JavaMethodReference {
    private final JavaNormalClass mClass;
    private final String mName;
    private final ImmutableList<JavaType> mParameters;
    private final JavaVoidableType mReturnType;

    public JavaMethodReference(
            JavaNormalClass javaClass,
            String methodName,
            ImmutableList<JavaType> parameters,
            JavaVoidableType returnType) {
        ensureNonNull(javaClass, parameters, returnType);
        ensureValidArguments(isValidVariableName(methodName));
        mClass = javaClass;
        mName = methodName;
        mParameters = parameters;
        mReturnType = returnType;
    }

    public JavaNormalClass getJavaClass() {
        return mClass;
    }

    public String getName() {
        return mName;
    }

    public ImmutableList<JavaType> getParameters() {
        return mParameters;
    }

    public JavaVoidableType getReturnType() {
        return mReturnType;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaMethodReference that &&
                mClass.equals(that.mClass) &&
                mName.equals(that.mName) &&
                mParameters.equals(that.mParameters) &&
                mReturnType.equals(that.mReturnType);
    }
}
