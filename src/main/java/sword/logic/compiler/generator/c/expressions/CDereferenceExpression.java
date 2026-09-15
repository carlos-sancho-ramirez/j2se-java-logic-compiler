package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CDereferenceExpression implements CExpression {
    private final CExpression mExpression;

    public CDereferenceExpression(CExpression expression) {
        ensureNonNull(expression);
        mExpression = expression;
    }

    @Override
    public String getText() {
        return "&" + mExpression.getText();
    }
}
