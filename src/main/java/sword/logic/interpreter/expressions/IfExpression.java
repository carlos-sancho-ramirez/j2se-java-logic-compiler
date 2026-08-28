package sword.logic.interpreter.expressions;

import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.Keywords;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IfExpression implements Expression {
    private final Token mIfToken;
    private final Token mThenToken;
    private final Token mElseToken;
    private final Expression mCondition;
    private final Expression mThenClause;
    private final Expression mElseClause;

    public IfExpression(
            Token ifToken,
            Token thenToken,
            Token elseToken,
            Expression condition,
            Expression thenClause,
            Expression elseClause) {
        ensureValidArguments(
                ifToken.getText() == Keywords.IF &&
                thenToken.getText() == Keywords.THEN ||
                elseToken.getText() == Keywords.ELSE);
        ensureNonNull(condition, thenClause, elseClause);
        mIfToken = ifToken;
        mThenToken = thenToken;
        mElseToken = elseToken;
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mCondition.findAllFinders(outMap, scope);
        mThenClause.findAllFinders(outMap, scope.whenTrue(mCondition));
        mElseClause.findAllFinders(outMap, scope.whenFalse(mCondition));
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mCondition.dependencies()
                .addAll(mThenClause.dependencies())
                .addAll(mElseClause.dependencies());
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        final Type rawConditionType = resolvedExpressions.get(mCondition, null);
        final Type thenType = resolvedExpressions.get(mThenClause, null);
        final Type elseType = resolvedExpressions.get(mElseClause, null);

        if (rawConditionType != null && thenType != null && elseType != null) {
            if (rawConditionType instanceof EnumType conditionType && conditionType.isBooleanType()) {
                final Type result = thenType.getUnion(elseType);
                if (result != null) {
                    return result;
                }
                else {
                    throw new SemanticErrorException("Incompatible types", mThenToken.getLine(), mThenToken.getColumn());
                }
            }
            else {
                throw new SemanticErrorException("Condition should result in a Boolean expression", mThenToken.getLine(), mThenToken.getColumn());
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
    public sword.logic.expressions.IfExpression untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap) {
        final sword.logic.expressions.IfExpression result = new sword.logic.expressions.IfExpression(
                mCondition.untokenize(typeAliasResolverMap, resolvedExpressions, outExpressionMap),
                mThenClause.untokenize(typeAliasResolverMap, resolvedExpressions, outExpressionMap),
                mElseClause.untokenize(typeAliasResolverMap, resolvedExpressions, outExpressionMap));
        outExpressionMap.put(this, result);
        return result;
    }
}
