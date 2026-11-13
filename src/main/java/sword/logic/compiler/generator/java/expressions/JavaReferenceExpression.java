package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaReferenceExpression implements JavaExpression {
    private final String mReference;

    public JavaReferenceExpression(String reference) {
        ensureNonNull(reference);
        mReference = reference;
    }

    @Override
    public String getText() {
        return mReference;
    }
}
