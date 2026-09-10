package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CArrayValueAtExpression implements CAssignableExpression {
    private final CExpression mReference;
    private final CExpression mIndex;

    public CArrayValueAtExpression(CExpression reference, CExpression index) {
        ensureNonNull(reference, index);
        mReference = reference;
        mIndex = index;
    }

    @Override
    public String getText() {
        return ((mReference instanceof CReferenceExpression)? mReference.getText() : "(" + mReference.getText() + ")") +
                "[" + mIndex.getText() + "]";
    }
}
