package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.interpreter.types.FunctionParameter;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionDefinitionExpression implements Expression {
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionDefinitionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureValidArguments(!parameters.isEmpty() && ImmutableListExtensions.noneRepeated(parameters.map(p -> p.getName().getText())));
        mParameters = parameters;
        mBody = body;
    }
}
