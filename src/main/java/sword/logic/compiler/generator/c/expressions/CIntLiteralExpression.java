package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.IntegerLiteralOperations.validIntegerLiteral;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CIntLiteralExpression implements CExpression {
    private final String mValue;

    public CIntLiteralExpression(String value) {
        ensureValidArguments(validIntegerLiteral(value));
        mValue = value;
    }

    @Override
    public String getText() {
        return mValue;
    }
}
