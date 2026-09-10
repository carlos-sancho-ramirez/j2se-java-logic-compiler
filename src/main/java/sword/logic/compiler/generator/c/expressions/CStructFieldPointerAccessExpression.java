package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CStructFieldPointerAccessExpression implements CAssignableExpression {
    private final CExpression mStructReference;
    private final String mFieldName;

    public CStructFieldPointerAccessExpression(CExpression structReference, String fieldName) {
        ensureNonNull(structReference, fieldName);
        mStructReference = structReference;
        mFieldName = fieldName;
    }

    @Override
    public String getText() {
        return mStructReference.getText() + "->" + mFieldName;
    }
}
