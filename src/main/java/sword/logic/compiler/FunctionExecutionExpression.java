package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionExecutionExpression implements Expression {
    private final Expression mFunction;
    private final ImmutableList<Expression> mParameters;

    public FunctionExecutionExpression(Expression function, ImmutableList<Expression> parameters) {
        ensureNonNull(function, parameters);
        ensureValidArguments(!(function instanceof FunctionExpression funcExp) ||
                funcExp.getParameters().size() == parameters.size() &&
                parameters.indexes().allMatch(i -> funcExp.getParameters().valueAt(i).equals(parameters.valueAt(i).resultingType())));

        mFunction = function;
        mParameters = parameters;
    }

    public Expression getFunction() {
        return mFunction;
    }

    public ImmutableList<Expression> getParameters() {
        return mParameters;
    }

    @Override
    public Type resultingType() {
        return (mFunction.resultingType() instanceof FunctionType funcType)?
                funcType.getResultType() : UnknownType.getInstance();
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (mFunction instanceof FunctionExpression funcExp) {
            final Expression newFunction = mFunction.resultTo(new FunctionType(funcExp.resultingType().getParameterTypes(), type));
            return (newFunction == mFunction)? this : new FunctionExecutionExpression(newFunction, mParameters);
        }
        else {
            final Expression newFunction = mFunction.resultTo(new FunctionType(mParameters.map(Expression::resultingType), type));
            return (newFunction == mFunction)? this : new FunctionExecutionExpression(newFunction, mParameters);
        }
    }
}
