package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExecutionExpression implements Expression {
    private final Expression mFunction;
    private final ImmutableList<Expression> mParameters;

    public FunctionExecutionExpression(Expression function, ImmutableList<Expression> parameters) {
        ensureNonNull(function, parameters);
        mFunction = function;
        mParameters = parameters;
    }

    public Expression getFunction() {
        return mFunction;
    }

    public ImmutableList<Expression> getParameters() {
        return mParameters;
    }
}
