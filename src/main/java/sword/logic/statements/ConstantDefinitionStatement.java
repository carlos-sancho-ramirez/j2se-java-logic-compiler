package sword.logic.statements;

import sword.logic.expressions.Expression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ConstantDefinitionStatement implements Statement {
    public static boolean validConstantName(String name) {
        if (name != null && !name.isEmpty() && name.charAt(0) >= 'a' && name.charAt(0) <= 'z') {
            final int length = name.length();
            for (int i = 1; i < length; i++) {
                final char ch = name.charAt(i);
                if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')) {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
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
