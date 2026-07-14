package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionType implements Type {
    private final ImmutableList<Type> mParameterTypes;
    private final Type mResultType;

    public FunctionType(ImmutableList<Type> parameterTypes, Type resultType) {
        ensureNonNull(parameterTypes, resultType);
        mParameterTypes = parameterTypes;
        mResultType = resultType;
    }

    public ImmutableList<Type> getParameterTypes() {
        return mParameterTypes;
    }

    public Type getResultType() {
        return mResultType;
    }
}
