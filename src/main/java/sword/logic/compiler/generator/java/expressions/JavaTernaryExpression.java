package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaTernaryExpression implements JavaExpression {
    private final JavaExpression mCondition;
    private final JavaExpression mThenClause;
    private final JavaExpression mElseClause;

    public JavaTernaryExpression(JavaExpression condition, JavaExpression thenClause, JavaExpression elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }

    @Override
    public String getText() {
        // TODO: Adjust parentheses
        return "(" + mCondition.getText() + ")? " + mThenClause.getText() + " : " + mElseClause.getText();
    }
}
