package sword.logic.syntax_tree.statements;

import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.expressions.Expression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ConstantDefinitionStatement implements Statement {
    private final Token mName;
    private final Expression mExpression;

    public ConstantDefinitionStatement(Token name, Expression expression) {
        ensureNonNull(name, expression);
        mName = name;
        mExpression = expression;
    }

    public Token getName() {
        return mName;
    }

    public Expression getExpression() {
        return mExpression;
    }
}
