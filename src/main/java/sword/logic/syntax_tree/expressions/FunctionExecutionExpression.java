package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.types.FunctionType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionExecutionExpression implements Expression {
    private final Expression mFunction;
    private final ImmutableList<Expression> mParameters;

    public FunctionExecutionExpression(Expression function, ImmutableList<Expression> parameters) {
        ensureNonNull(function, parameters);
        ensureValidArguments(!(function instanceof FunctionExpression funcExp) ||
                funcExp.getParameters().size() == parameters.size() &&
                parameters.indexes().allMatch(i -> funcExp.getParameters().valueAt(i).equals(parameters.valueAt(i).requiredType())));

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
    public Type requiredType() {
        return (mFunction.requiredType() instanceof FunctionType funcType)?
                funcType.getResultType() : UnknownType.getInstance();
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (mFunction instanceof FunctionExpression funcExp) {
            final Expression newFunction = mFunction.requiresType(new FunctionType(funcExp.requiredType().getParameterTypes(), type));
            return (newFunction == mFunction)? this : new FunctionExecutionExpression(newFunction, mParameters);
        }
        else {
            final Expression newFunction = mFunction.requiresType(new FunctionType(mParameters.map(Expression::requiredType), type));
            return (newFunction == mFunction)? this : new FunctionExecutionExpression(newFunction, mParameters);
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mFunction.resolveReferences(knownTargets);
        for (Expression param : mParameters) {
            param.resolveReferences(knownTargets);
        }
    }
}
