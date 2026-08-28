package sword.logic.expressions;

import sword.collections.ImmutableSet;

public interface Expression {
    ImmutableSet<String> dependencies();

    default boolean isIndependent() {
        return dependencies().isEmpty();
    }
}
