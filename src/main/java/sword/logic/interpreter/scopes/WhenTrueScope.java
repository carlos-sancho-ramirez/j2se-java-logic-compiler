package sword.logic.interpreter.scopes;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.ImpossibleSituationException;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.syntax_tree.Token;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

/**
 * Scope holding a expression, that should result in a Boolean expression,
 * that allow restricting the existing scope assuming that the expression will return true.
 */
public final class WhenTrueScope extends AbstractScope {
    private final AbstractScope mParent;
    private final Expression mCondition;

    WhenTrueScope(AbstractScope parent, Expression condition) {
        ensureNonNull(parent, condition);
        mParent = parent;
        mCondition = condition;
    }

    @Override
    public boolean knowsConstantName(String constantName) {
        return mParent.knowsConstantName(constantName);
    }

    @Override
    public EnumType resolveEnumValue(Token value) throws UnresolvedEnumValueException {
        return mParent.resolveEnumValue(value);
    }

    @Override
    public Type resolveTypeAlias(Token typeAlias) throws UnresolvedTypeReferenceException {
        return mParent.resolveTypeAlias(typeAlias);
    }

    @Override
    Type resolveDefinedType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        return mParent.resolveDefinedType(constantName, resolvedExpressions);
    }

    @Override
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        if (resolvedExpressions.containsKey(mCondition)) {
            final Type definedType = resolveDefinedType(constantName, resolvedExpressions);
            final EnumType alwaysTrueType = BuiltInScope.getInstance().getAlwaysTrueType();
            try {
                final ImmutableMap<String, Type> newRestrictionMap = mCondition.restrictionMap(alwaysTrueType, resolvedExpressions, restrictionMap);
                if (newRestrictionMap == null) {
                    return null;
                }
                else {
                    ensureValidState(!newRestrictionMap.containsKey(constantName.getText()) || definedType.canFit(newRestrictionMap.get(constantName.getText())));
                    return mParent.resolveType(constantName, resolvedExpressions, newRestrictionMap);
                }
            }
            catch (ImpossibleSituationException e) {
                throw new RuntimeException("Unable to resolveType", e);
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        return resolveType(constantName, resolvedExpressions, ImmutableHashMap.empty());
    }
}
