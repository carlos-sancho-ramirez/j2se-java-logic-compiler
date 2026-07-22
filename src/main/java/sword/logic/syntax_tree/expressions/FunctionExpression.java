package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.types.FunctionType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExpression implements Expression {
    private final FunctionType mRequiredType;
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureNonNull(parameters, body);
        mParameters = parameters;
        mBody = body;
        mRequiredType = new FunctionType(parameters.map(FunctionParameter::getType), body.requiredType());
    }

    public ImmutableList<FunctionParameter> getParameters() {
        return mParameters;
    }

    public Expression getBody() {
        return mBody;
    }

    @Override
    public FunctionType requiredType() {
        return mRequiredType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof FunctionType funcType) {
            // TODO: We should handle the function parameters as well
            final Expression newBody = mBody.requiresType(funcType.getResultType());
            return (newBody == mBody)? this : new FunctionExpression(mParameters, newBody);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is a Function");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        final MutableMap<String, ReferenceTarget> newTargets = knownTargets.mutate();
        for (FunctionParameter param : mParameters) {
            newTargets.put(param.getName().getText(), param);
        }

        mBody.resolveReferences(newTargets);
    }
}
