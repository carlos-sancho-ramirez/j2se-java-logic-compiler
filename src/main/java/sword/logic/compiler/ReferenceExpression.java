package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ReferenceExpression implements Expression {
    private final Token mReference;

    public ReferenceExpression(Token reference) {
        ensureNonNull(reference);
        mReference = reference;
    }

    public Token getReference() {
        return mReference;
    }
}
