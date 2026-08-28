package sword.logic.statements;

import sword.logic.expressions.Expression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ConstantDefinitionStatement implements Statement {
    public static boolean validConstantName(String name) {
        return name.charAt(0) >= 'a' && name.charAt(0) <= 'z';
    }

    private final String mName;
    private final Expression mExpression;

    public ConstantDefinitionStatement(String name, Expression expression) {
        ensureValidArguments(validConstantName(name));
        ensureNonNull(expression);
        mName = name;
        mExpression = expression;
    }

    @Override
    public String getName() {
        return mName;
    }

    public Expression getExpression() {
        return mExpression;
    }
}
