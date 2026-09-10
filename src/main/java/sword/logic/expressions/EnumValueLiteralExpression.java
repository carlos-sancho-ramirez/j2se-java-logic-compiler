package sword.logic.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class EnumValueLiteralExpression implements LiteralExpression {
    private final String mLiteral;

    public EnumValueLiteralExpression(String literal) {
        ensureValidArguments(literal.charAt(0) >= 'A' && literal.charAt(0) <= 'Z');
        mLiteral = literal;
    }

    public String getValue() {
        return mLiteral;
    }
}
