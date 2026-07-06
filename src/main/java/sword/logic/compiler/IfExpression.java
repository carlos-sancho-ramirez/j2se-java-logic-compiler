package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class IfExpression implements Expression {
    private final Expression mCondition;
    private final Expression mThenClause;
    private final Expression mElseClause;

    public IfExpression(Expression condition, Expression thenClause, Expression elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }
}
