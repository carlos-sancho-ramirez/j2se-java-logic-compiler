package sword.logic.types;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionType implements Type {
    private final ImmutableList<Type> mParameterTypes;
    private final Type mResultType;

    public FunctionType(ImmutableList<Type> parameterTypes, Type resultType) {
        ensureValidArguments(!parameterTypes.isEmpty());
        ensureNonNull(resultType);
        mParameterTypes = parameterTypes;
        mResultType = resultType;
    }

    public ImmutableList<Type> getParameterTypes() {
        return mParameterTypes;
    }

    public Type getResultType() {
        return mResultType;
    }

    @Override
    public Type getIntersection(Type other) {
        if (other instanceof FunctionType that) {
            // So far I leave the parameters invariants and the resulting type covariant. Maybe we should rethink this.
            if (mParameterTypes.equals(that.getParameterTypes())) {
                final Type newResultingType = mResultType.getIntersection(that.getResultType());
                return mResultType.equals(newResultingType)? this :
                        that.getResultType().equals(newResultingType)? that :
                        (newResultingType == null)? null :
                        new FunctionType(mParameterTypes, newResultingType);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Type getUnion(Type other) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public boolean canFit(Type type) {
        // So far I leave the parameters invariants and the resulting type covariant. Maybe we should rethink this.
        return type instanceof FunctionType newType && mParameterTypes.equals(newType.getParameterTypes()) && mResultType.canFit(type);
    }

    @Override
    public int hashCode() {
        return mResultType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof FunctionType that &&
                mParameterTypes.equals(that.mParameterTypes) &&
                mResultType.equals(that.mResultType);
    }
}
