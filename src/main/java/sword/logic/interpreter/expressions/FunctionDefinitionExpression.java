package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.FunctionParameter;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.FunctionType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class FunctionDefinitionExpression implements Expression {
    private final ImmutableList<FunctionParameter> mParameters;
    private final Expression mBody;

    public FunctionDefinitionExpression(ImmutableList<FunctionParameter> parameters, Expression body) {
        ensureValidArguments(!parameters.isEmpty() && ImmutableListExtensions.noneRepeated(parameters.map(p -> p.getName().getText())));
        mParameters = parameters;
        mBody = body;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        final Scope newScope = scope.createSubscopeWithParameters(mParameters);
        mBody.findAllFinders(outMap, newScope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mBody.dependencies();
        for (FunctionParameter param : mParameters) {
            result = result.remove(param.getName().getText());
        }

        return result;
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        final Type bodyType = resolvedExpressions.get(mBody, null);
        if (bodyType != null) {
            final Scope newScope = scope.createSubscopeWithParameters(mParameters);
            final ImmutableList<Type> parameterTypes = ImmutableListExtensions.mapThrowing(mParameters, param -> param.getType().resolve(newScope));
            return new FunctionType(parameterTypes, bodyType);
        }
        else {
            return null;
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
    public sword.logic.expressions.FunctionDefinitionExpression untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap) {
        final sword.logic.expressions.FunctionDefinitionExpression result = new sword.logic.expressions.FunctionDefinitionExpression(mParameters.map(p -> p.untokenize(typeAliasResolverMap.get(this))), mBody.untokenize(typeAliasResolverMap, resolvedExpressions, outExpressionMap));
        outExpressionMap.put(this, result);
        return result;
    }
}
