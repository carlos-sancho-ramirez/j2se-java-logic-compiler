package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

public interface Expression {
    /**
     * Type that this expression is required to result to according to the rest of the expression including this one.
     * <p>
     * This can be {@link UnknownType} instance if not enough information is known.
     * @return The type that this expression is resulting to.
     */
    Type requiredType();

    /**
     * Transforms this expression to ensure that its requiredType is equivalent to the given one.
     * <p>
     * If UnknownType is given as parameter, or inside other types, like in arrays,
     * it will be understood as a wildcard. So, if the current expression results
     * to something and it is forced to the UnknownType, the current type will be
     * the resulting one.
     * <p>
     * For now, this method will ignore any conflict between integer ranges, if any.
     * @param type Type that we want this expression to result into.
     * @return The same instance if the type matches or the given type is known, or a new instance with the resulting type changed.
     */
    Expression requiresType(Type type) throws TypeMismatchException;

    /**
     * Resolve all references within this expression and throws an exception if an unresolved reference is found and no target fits on it.
     *
     * @param knownTargets Available targets that can be applied to references.
     * @throws UnresolvedReferenceException If an unresolved reference is found and none of the given targets fits on it.
     */
    void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException;
}
