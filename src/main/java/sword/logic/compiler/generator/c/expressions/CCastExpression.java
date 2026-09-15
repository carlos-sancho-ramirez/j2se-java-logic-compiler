package sword.logic.compiler.generator.c.expressions;

import sword.logic.compiler.generator.c.types.CTypeDeclaration;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CCastExpression implements CAssignableExpression {
    private final CTypeDeclaration mType;
    private final CExpression mExpression;

    public CCastExpression(CTypeDeclaration type, CExpression expression) {
        ensureNonNull(type, expression);
        mType = type;
        mExpression = expression;
    }

    @Override
    public String getText() {
        return "(" + mType.getText() + ") " + mExpression.getText();
    }
}
