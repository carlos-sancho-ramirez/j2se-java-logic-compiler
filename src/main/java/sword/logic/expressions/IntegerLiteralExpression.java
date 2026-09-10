package sword.logic.expressions;

import sword.logic.compiler.IntegerLiteralOperations;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IntegerLiteralExpression implements LiteralExpression {
    private final String mLiteral;

    public IntegerLiteralExpression(String literal) {
        ensureValidArguments(IntegerLiteralOperations.validIntegerLiteral(literal));
        mLiteral = literal;
    }

    public String getLiteral() {
        return mLiteral;
    }
}
