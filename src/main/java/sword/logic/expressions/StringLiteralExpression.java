package sword.logic.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StringLiteralExpression implements LiteralExpression {
    private final String mLiteral;

    public StringLiteralExpression(String literal) {
        ensureValidArguments(literal.charAt(0) == '"' && literal.charAt(literal.length() - 1) == '"');
        mLiteral = literal;
    }
}
