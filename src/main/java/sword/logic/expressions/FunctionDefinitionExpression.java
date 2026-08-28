package sword.logic.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableSet;
import sword.logic.types.FunctionParameter;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionDefinitionExpression implements Expression {
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionDefinitionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureValidArguments(!parameters.isEmpty() && ImmutableListExtensions.noneRepeated(parameters.map(FunctionParameter::getName)));
        mParameters = parameters;
        mBody = body;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mBody.dependencies();
        for (FunctionParameter param : mParameters) {
            result = result.remove(param.getName());
        }

        return result;
    }
}
