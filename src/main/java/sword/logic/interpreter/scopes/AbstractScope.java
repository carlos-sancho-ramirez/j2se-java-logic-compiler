package sword.logic.interpreter.scopes;

import sword.collections.ImmutableMap;
import sword.collections.ImmutableTransformable;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.statements.Statement;
import sword.logic.interpreter.type.definitions.FunctionParameter;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

abstract class AbstractScope implements Scope {

    /**
     * Resolve the type for the given constant name according to the constant definitions and type alias declared in this scope.
     * <p>
     * This method will explicitly ignore any type restriction and will only check the statements defined.
     * <p>
     * In case the type cannot be resolved because other expressions must be resolved first, then this method
     * will return null. Consumers of this method must deal with the possibility of receiving null.
     *
     * @param constantName Name for the constant that we want the type from.
     * @param resolvedExpressions All the expressions already resolved.
     * @return The type of that constant according to the statements in the scope, or null if it cannot be resolved yet.
     * @throws UnresolvedTypeReferenceException In case a type reference cannot be resolved.
     * @throws UnresolvedReferenceException In case the given constant name does not exist in this scope.
     */
    abstract Type resolveDefinedType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException;

    abstract Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException;

    public Scope whenTrue(Expression condition) {
        return new WhenTrueScope(this, condition);
    }

    public Scope whenFalse(Expression condition) {
        return new WhenFalseScope(this, condition);
    }

    public Scope createSubscopeWithParameters(ImmutableTransformable<FunctionParameter> parameters) {
        return new FunctionParametersHolderScope(this, parameters.toSet());
    }

    public Scope createSubscopeWithStatements(ImmutableTransformable<Statement> statements) {
        return new StatementsHolderScope(this, statements.toSet());
    }
}
