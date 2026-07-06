package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IntegerLiteralExpression implements Expression {
    private final Token mLiteral;

    public IntegerLiteralExpression(Token literal) {
        ensureValidArguments(literal.getText().charAt(0) >= '0' && literal.getText().charAt(0) <= '9');
        mLiteral = literal;
    }
}
