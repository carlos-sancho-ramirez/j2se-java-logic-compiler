package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CReferenceExpression implements CAssignableExpression {
    private final String mReference;

    public CReferenceExpression(String reference) {
        ensureValidArguments(!reference.isEmpty() && reference.charAt(0) >= 'a' && reference.charAt(0) <= 'z');
        mReference = reference;
    }

    @Override
    public String getText() {
        return mReference;
    }
}
