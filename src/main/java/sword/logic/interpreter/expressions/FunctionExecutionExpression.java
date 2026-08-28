package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.FunctionType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionExecutionExpression implements Expression {
    private final Token mOpenParentheses;
    private final Expression mFunction;
    private final ImmutableList<Expression> mParameters;

    public FunctionExecutionExpression(Token openParentheses, Expression function, ImmutableList<Expression> parameters) {
        ensureNonNull(openParentheses, function, parameters);
        mOpenParentheses = openParentheses;
        mFunction = function;
        mParameters = parameters;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mFunction.findAllFinders(outMap, scope);
        for (Expression parameter : mParameters) {
            parameter.findAllFinders(outMap, scope);
        }
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mFunction.dependencies();
        for (Expression param : mParameters) {
            result = result.addAll(param.dependencies());
        }

        return result;
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        if (!resolvedExpressions.containsKey(mFunction) || !mParameters.allMatch(resolvedExpressions::containsKey)) {
            return null;
        }
        else if (resolvedExpressions.get(mFunction) instanceof FunctionType funcType) {
            // TODO: We should narrow the resulting type based on the given parameters
            return funcType.getResultType();
        }
        else {
            throw new SemanticErrorException("Expected function", mOpenParentheses.getLine(), mOpenParentheses.getColumn());
        }
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public sword.logic.expressions.FunctionExecutionExpression untokenize(ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap, Map<Expression, Type> resolvedExpressions) {
        return new sword.logic.expressions.FunctionExecutionExpression(
                mFunction.untokenize(typeAliasResolverMap, resolvedExpressions),
                mParameters.map(p -> p.untokenize(typeAliasResolverMap, resolvedExpressions)));
    }
}
