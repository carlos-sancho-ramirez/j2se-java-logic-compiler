package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExpression implements Expression {
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureNonNull(parameters, body);
        mParameters = parameters;
        mBody = body;
    }
}
