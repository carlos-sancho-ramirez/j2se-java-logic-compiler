package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class BooleanLiteralExpression implements Expression {
    private final Token mToken;
    private final boolean mValue;

    public BooleanLiteralExpression(Token token, boolean value) {
        ensureNonNull(token);
        mToken = token;
        mValue = value;
    }
}
