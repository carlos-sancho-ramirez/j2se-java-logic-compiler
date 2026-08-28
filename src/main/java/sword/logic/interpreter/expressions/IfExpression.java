package sword.logic.interpreter.expressions;

import sword.logic.interpreter.Keywords;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IfExpression implements Expression {
    private final Token mIfToken;
    private final Token mThenToken;
    private final Token mElseToken;
    private final Expression mCondition;
    private final Expression mThenClause;
    private final Expression mElseClause;

    public IfExpression(
            Token ifToken,
            Token thenToken,
            Token elseToken,
            Expression condition,
            Expression thenClause,
            Expression elseClause) {
        ensureValidArguments(
                ifToken.getText() == Keywords.IF &&
                thenToken.getText() == Keywords.THEN ||
                elseToken.getText() == Keywords.ELSE);
        ensureNonNull(condition, thenClause, elseClause);
        mIfToken = ifToken;
        mThenToken = thenToken;
        mElseToken = elseToken;
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }
}
