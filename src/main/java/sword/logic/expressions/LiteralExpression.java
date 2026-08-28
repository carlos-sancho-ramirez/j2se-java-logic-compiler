package sword.logic.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableSet;

public interface LiteralExpression extends Expression {
    @Override
    default ImmutableSet<String> dependencies() {
        return ImmutableHashSet.empty();
    }
}
