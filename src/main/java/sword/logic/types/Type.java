package sword.logic.types;

public interface Type {
    /**
     * Returns the intersection of the current type and the given one, or null
     * if the intersection is not possible because they do not share any possible values.
     * @param other The other Type to intersect this one to.
     * @return the intersection of the current type and the given one, or null
     *         if the intersection is not possible because they do not share any possible value.
     */
    Type getIntersection(Type other);

    /**
     * Returns the union of the current type and the given one, or null
     * if the union is not possible because they do not share its type.
     * @param other The other Type to unify this one to.
     * @return the union of the current type and the given one, or null
     *         if the union is not possible because they do not share type.
     */
    Type getUnion(Type other);

    /**
     * Whether any value respecting the given type could be assigned to this type.
     * @param newType New type to be evaluated
     * @return Whether the new type fits inside the current one.
     */
    default boolean canFit(Type newType) {
        return newType.equals(getIntersection(newType));
    }
}
