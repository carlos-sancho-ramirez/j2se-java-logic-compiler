package sword.logic.interpreter.scopes;

import sword.collections.ImmutableTransformable;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.statements.Statement;
import sword.logic.interpreter.type.definitions.FunctionParameter;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

public interface Scope extends TypeAliasResolver {
    /**
     * Whether the given constant name is found within this scope.
     * @param constantName Name for the constant.
     * @return Whether the constant name is defined in this scope.
     */
    boolean knowsConstantName(String constantName);

    /**
     * Finds the enum type where this value belongs to.
     * @param value Value to be found among the defined enums.
     * @return The enum type that holds the value.
     * @throws UnresolvedEnumValueException If the value does not belong to any existing enum.
     */
    EnumType resolveEnumValue(Token value) throws UnresolvedEnumValueException;

    /**
     * Resolves the type of the given constant name.
     * <p>
     * This method will rely on the already resolved expression types.
     * If an expression type is required but the resolvedExpressions parameter
     * does not include it, this method will return null indicating that
     * it cannot be resolved now, but it may whenever the required expression
     * type has been resolved.
     * <p>
     * This method is also responsible of throwing exceptions if the given
     * constantName is not present in the scope.
     *
     * @param constantName Name for the constant that we need the type from.
     * @param resolvedExpressions All resolved expression types so far.
     * @return The resolved type, or null if it cannot be resolved yet.
     * @throws UnresolvedTypeReferenceException In case the constant points to a type alias that has not been defined.
     * @throws UnresolvedReferenceException In case the constant name is not present in this scope.
     */
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException;

    Scope whenTrue(Expression condition);
    Scope whenFalse(Expression condition);
    Scope createSubscopeWithParameters(ImmutableTransformable<FunctionParameter> parameters);
    Scope createSubscopeWithStatements(ImmutableTransformable<Statement> statements);
}
