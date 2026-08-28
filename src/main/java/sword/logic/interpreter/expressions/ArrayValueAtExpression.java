package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayValueAtExpression implements Expression {
    private final Token mOpenBracket;
    private final Token mCloseBracket;
    private final Expression mArray;
    private final Expression mIndex;

    public ArrayValueAtExpression(Token openBracket, Token closeBracket, Expression array, Expression index) {
        ensureNonNull(openBracket, closeBracket, array, index);
        mOpenBracket = openBracket;
        mCloseBracket = closeBracket;
        mArray = array;
        mIndex = index;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mArray.findAllFinders(outMap, scope);
        mIndex.findAllFinders(outMap, scope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mArray.dependencies().addAll(mIndex.dependencies());
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        final Type rawArrayType = resolvedExpressions.get(mArray, null);
        final Type rawIndexType = resolvedExpressions.get(mIndex, null);
        if (rawArrayType != null && rawIndexType != null) {
            if (rawArrayType instanceof ArrayType) {
                if (rawIndexType instanceof IntType) {
                    final ImmutableSet<String> arrayDependencies = mArray.dependencies();
                    final ImmutableMap.Builder<String, Type> restrictionMapBuilder = new ImmutableHashMap.Builder<>();
                    for (String dependency : arrayDependencies) {
                        final Type newType = scope.resolveType(new Token(dependency), resolvedExpressions);
                        if (newType == null) {
                            return null;
                        }
                        else {
                            restrictionMapBuilder.put(dependency, newType);
                        }
                    }
                    final ImmutableMap<String, Type> restrictionMap = restrictionMapBuilder.build();
                    final ArrayType arrayType = (ArrayType) mArray.resolveType(restrictionMap);

                    final ImmutableSet<String> indexDependencies = mIndex.dependencies();
                    final ImmutableMap.Builder<String, Type> indexRestrictionMapBuilder = new ImmutableHashMap.Builder<>();
                    for (String dependency : indexDependencies) {
                        final Type newType = scope.resolveType(new Token(dependency), resolvedExpressions);
                        if (newType == null) {
                            return null;
                        }
                        else {
                            indexRestrictionMapBuilder.put(dependency, newType);
                        }
                    }
                    final ImmutableMap<String, Type> indexRestrictionMap = indexRestrictionMapBuilder.build();
                    final IntType indexType = (IntType) mIndex.resolveType(indexRestrictionMap);

                    if (!indexType.getRanges().first().getMin().equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(indexType.getRanges().first().getMin(), TypeConstants.zeroText)) {
                        if (indexType.getRanges().last().getMax().equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerThan(indexType.getRanges().last().getMax(), arrayType.getLengthType().getRanges().first().getMin())) {
                            return arrayType.getItemType();
                        }
                        else {
                            throw new SemanticErrorException("Index should not take an equal or higher value than the array length", mCloseBracket.getLine(), mCloseBracket.getColumn());
                        }
                    }
                    else {
                        throw new SemanticErrorException("No negative numbers allowed for index", mCloseBracket.getLine(), mCloseBracket.getColumn());
                    }
                }
                else {
                    throw new SemanticErrorException("Expected number expression for index", mCloseBracket.getLine(), mCloseBracket.getColumn());
                }
            }
            else {
                throw new SemanticErrorException("Expected array", mOpenBracket.getLine(), mOpenBracket.getColumn());
            }
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
    public sword.logic.expressions.Expression untokenize(ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap, Map<Expression, Type> resolvedExpressions) {
        return new sword.logic.expressions.ArrayValueAtExpression(mArray.untokenize(typeAliasResolverMap, resolvedExpressions), mIndex.untokenize(typeAliasResolverMap, resolvedExpressions));
    }
}
