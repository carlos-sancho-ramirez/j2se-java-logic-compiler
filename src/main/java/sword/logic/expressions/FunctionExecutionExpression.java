package sword.logic.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExecutionExpression implements Expression {
    private final Expression mFunction;
    private final ImmutableList<Expression> mParameters;

    public FunctionExecutionExpression(Expression function, ImmutableList<Expression> parameters) {
        ensureNonNull(function, parameters);
        mFunction = function;
        mParameters = parameters;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mFunction.dependencies();
        for (Expression param : mParameters) {
            result = result.addAll(param.dependencies());
        }

        return result;
    }
}
