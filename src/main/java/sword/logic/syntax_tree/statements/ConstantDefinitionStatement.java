package sword.logic.syntax_tree.statements;

import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.expressions.Expression;
import sword.logic.syntax_tree.expressions.ReferenceTarget;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ConstantDefinitionStatement implements Statement, ReferenceTarget {
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

    @Override
    public Type getType() {
        return mExpression.requiredType();
    }
}
