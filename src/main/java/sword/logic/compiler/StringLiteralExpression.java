package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StringLiteralExpression implements Expression {
    private final Token mLiteral;

    public StringLiteralExpression(Token literal) {
        ensureValidArguments(literal.getText().charAt(0) == '"');
        mLiteral = literal;
    }

    public Token getLiteral() {
        return mLiteral;
    }
}
