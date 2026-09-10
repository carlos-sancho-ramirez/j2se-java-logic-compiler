package sword.logic.expressions;

import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class IfExpression implements Expression {
    private final Expression mCondition;
    private final Expression mThenClause;
    private final Expression mElseClause;

    public IfExpression(
            Expression condition,
            Expression thenClause,
            Expression elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }

    public Expression getCondition() {
        return mCondition;
    }

    public Expression getThenClause() {
        return mThenClause;
    }

    public Expression getElseClause() {
        return mElseClause;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mCondition.dependencies()
                .addAll(mThenClause.dependencies())
                .addAll(mElseClause.dependencies());
    }
}
