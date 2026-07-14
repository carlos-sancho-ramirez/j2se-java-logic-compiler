package sword.logic.compiler;

public interface Expression {
    /**
     * Type that this expression is resulting to.
     * <p>
     * This can be {@link UnknownType} instance if not enough information is known.
     * @return The type that this expression is resulting to.
     */
    Type resultingType();

    /**
     * Transforms this expression to ensure that its resultingType is equivalent to the given one.
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
    Expression resultTo(Type type) throws TypeMismatchException;
}
