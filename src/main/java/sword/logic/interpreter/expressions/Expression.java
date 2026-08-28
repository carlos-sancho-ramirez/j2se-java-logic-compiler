package sword.logic.interpreter.expressions;

import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.ImpossibleSituationException;
import sword.logic.interpreter.Interpretation;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.types.Type;

public interface Expression extends Finder, Interpretation {

    ImmutableSet<String> dependencies();

    default boolean isIndependent() {
        return dependencies().isEmpty();
    }

    /**
     * Resolves the type for the current expression.
     * <p>
     * This method will not call itself in any expression inside.
     * Instead, it will check if the required expression is already resolved and included in the resolvedExpressions parameter.
     * This should avoid resolving the same expression more than once.
     * <p>
     * In case the type of this expression cannot be resolved because other expressions that this depends on are not yet resolved, this method will return null.
     * Callers of this method must handle the possibility to receive null as response and try to resolve this again later once the other expressions has been resolved.
     *
     * @param scope Scope where this expression is defined in.
     * @param resolvedExpressions All expressions already resolved. This method do not expect any null value in the map.
     *                            If the expression is not resolved yet, this method assumes that there will be no entry for that expression in the map.
     * @return The resolved type if it can be worked out, or null if it requires other expressions to be resolved first.
     * @throws UnresolvedTypeReferenceException In case there is a type mentioned but no definition for it.
     * @throws UnresolvedReferenceException In case there is a constant reference but no definition is found for it.
     * @throws SemanticErrorException In case the expression make no sense.
     */
    Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException;

    /**
     * Alternative way to retrieve the type.
     * <p>
     * This method is used internally to retrieve a type again when the types
     * have been restricted to get a more restricted type when possible.
     * <p>
     * This method assumes that the {@link #resolveType(Scope, Map)} has been
     * called first on this expression and all the other expressions this one
     * depends on. Because of that, only exceptions inheriting from
     * RuntimeException are thrown.
     *
     * @param restrictionMap Types that references can be use to resolve a type.
     *                       Callers of this methods must ensure that all
     *                       required references can be satisfied.
     * @return The restricted type resulting from this expression. Never null.
     */
    Type resolveType(ImmutableMap<String, Type> restrictionMap);

    /**
     * Composes a map of restrictions that the constants in the scope must satisfy in order to match the given result type.
     * <p>
     * This method will use the given restrictionMap to restrict even more the constants in case it is required.
     * The returned map will never change any existing type for a less restrictive one.
     * <p>
     * This method can also return null if it is not yet possible to work out the restrictions because there is still
     * expressions that are not resolved yet.
     *
     * @param resultType Type that we want this expression to result into.
     * @param resolvedExpressions All resolved expressions so far.
     * @param restrictionMap Restrictions already applied to the defined constants.
     * @return A new map of restrictions, or null if this cannot be evaluated yet.
     * @throws ImpossibleSituationException if it is not possible to reach the given value with the given restrictions.
     */
    ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws ImpossibleSituationException;
}
