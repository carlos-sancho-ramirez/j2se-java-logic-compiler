package sword.logic.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.types.EnumType.validEnumValueName;

public final class EnumValueLiteralExpression implements LiteralExpression {
    private final String mLiteral;

    public EnumValueLiteralExpression(String literal) {
        ensureValidArguments(validEnumValueName(literal));
        mLiteral = literal;
    }

    public String getValue() {
        return mLiteral;
    }
}
