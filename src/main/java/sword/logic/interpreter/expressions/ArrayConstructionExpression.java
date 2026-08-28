package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.EmptyArrayType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayConstructionExpression implements Expression {
    private final Token mType;
    private final ImmutableList<Expression> mParameters;

    public ArrayConstructionExpression(Token type, ImmutableList<Expression> parameters) {
        ensureValidArguments(type.getText() == TypeConstants.ARRAY_TYPE_TEXT);
        ensureNonNull(parameters);
        mType = type;
        mParameters = parameters;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        for (Expression param : mParameters) {
            param.findAllFinders(outMap, scope);
        }
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = ImmutableHashSet.empty();
        for (Expression param : mParameters) {
            result = result.addAll(param.dependencies());
        }

        return result;
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws SemanticErrorException {
        if (mParameters.isEmpty()) {
            return EmptyArrayType.getInstance();
        }
        else {
            if (!mParameters.allMatch(resolvedExpressions::containsKey)) {
                return null;
            }

            Type resultingItemType = resolvedExpressions.get(mParameters.first());
            for (Expression param : mParameters.skip(1)) {
                resultingItemType = resultingItemType.getUnion(resolvedExpressions.get(param));
                if (resultingItemType == null) {
                    throw new SemanticErrorException("Invalid mix of parameter types in the array construction", mType.getLine(), mType.getColumn());
                }
            }

            final String lengthText = "" + mParameters.size();
            return new ArrayType(IntType.withUniqueValue(lengthText), resultingItemType);
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
    public sword.logic.expressions.Expression untokenize(ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap, Map<Expression, Type> resolvedExpressions) {
        return new sword.logic.expressions.ArrayConstructionExpression(mParameters.map(param -> param.untokenize(typeAliasResolverMap, resolvedExpressions)));
    }
}
