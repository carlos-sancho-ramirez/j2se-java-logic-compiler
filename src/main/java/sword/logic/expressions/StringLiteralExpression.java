package sword.logic.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StringLiteralExpression implements LiteralExpression {
    private final String mLiteral;

    public StringLiteralExpression(String literal) {
        ensureValidArguments(literal.charAt(0) == '"' && literal.charAt(literal.length() - 1) == '"');
        mLiteral = literal;
    }

    public String getLiteral() {
        return mLiteral;
    }

    // I am centralising the array length work out in this method to be able to add escaped characters
    public int getArrayLength() {
        return mLiteral.length() - 2;
    }

    // I am centralising the character work out in this method to be able to add escaped characters.
    // Note that later we might want to return values like "\n", that is way the return type is a string
    // and not a single character.
    public String getCharAt(int position) {
        return "" + mLiteral.charAt(position + 1);
    }
}
