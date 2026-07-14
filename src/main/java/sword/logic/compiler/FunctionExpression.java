package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExpression implements Expression {
    private final FunctionType mResultingType;
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureNonNull(parameters, body);
        mParameters = parameters;
        mBody = body;
        mResultingType = new FunctionType(parameters.map(FunctionParameter::getType), body.resultingType());
    }

    public ImmutableList<FunctionParameter> getParameters() {
        return mParameters;
    }

    public Expression getBody() {
        return mBody;
    }

    @Override
    public FunctionType resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof FunctionType funcType) {
            // TODO: We should handle the function parameters as well
            final Expression newBody = mBody.resultTo(funcType.getResultType());
            return (newBody == mBody)? this : new FunctionExpression(mParameters, newBody);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is a Function");
        }
    }
}
