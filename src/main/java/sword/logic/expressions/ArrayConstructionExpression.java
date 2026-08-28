package sword.logic.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayConstructionExpression implements Expression {
    private final ImmutableList<Expression> mParameters;

    public ArrayConstructionExpression(ImmutableList<Expression> parameters) {
        ensureNonNull(parameters);
        mParameters = parameters;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = ImmutableHashSet.empty();
        for (Expression param : mParameters) {
            result = result.addAll(param.dependencies());
        }

        return result;
    }
}
