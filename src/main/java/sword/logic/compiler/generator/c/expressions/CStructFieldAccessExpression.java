package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CStructFieldAccessExpression implements CAssignableExpression {
    private final CExpression mStructReference;
    private final String mFieldName;

    public CStructFieldAccessExpression(CExpression structReference, String fieldName) {
        ensureNonNull(structReference, fieldName);
        ensureValidArguments(!fieldName.isEmpty() && fieldName.charAt(0) >= 'a' && fieldName.charAt(0) <= 'z');
        mStructReference = structReference;
        mFieldName = fieldName;
    }

    @Override
    public String getText() {
        return mStructReference.getText() + "." + mFieldName;
    }
}
