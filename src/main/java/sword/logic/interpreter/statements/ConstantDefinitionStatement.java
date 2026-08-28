package sword.logic.interpreter.statements;

import sword.logic.interpreter.expressions.Expression;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ConstantDefinitionStatement implements Statement {
    public static boolean validConstantName(String name) {
        return name.charAt(0) >= 'a' && name.charAt(0) <= 'z';
    }

    private final Token mName;
    private final Expression mExpression;

    public ConstantDefinitionStatement(Token name, Expression expression) {
        ensureValidArguments(validConstantName(name.getText()));
        ensureNonNull(expression);
        mName = name;
        mExpression = expression;
    }

    @Override
    public Token getName() {
        return mName;
    }

    public Expression getExpression() {
        return mExpression;
    }
}
