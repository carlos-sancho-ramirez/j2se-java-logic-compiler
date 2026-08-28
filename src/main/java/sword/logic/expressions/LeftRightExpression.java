package sword.logic.expressions;

import sword.collections.ImmutableSet;

public interface LeftRightExpression extends Expression {
    Expression getLeftExpression();
    Expression getRightExpression();

    @Override
    default ImmutableSet<String> dependencies() {
        return getLeftExpression().dependencies().addAll(getRightExpression().dependencies());
    }
}
