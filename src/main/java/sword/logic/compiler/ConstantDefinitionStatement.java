package sword.logic.compiler;

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
}
