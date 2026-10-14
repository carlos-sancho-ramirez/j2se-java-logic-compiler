package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.expressions.CExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CReturnStatement implements CInFunctionStatement {
    private final CExpression mExpression;

    public CReturnStatement(CExpression expression) {
        ensureNonNull(expression);
        mExpression = expression;
    }

    @Override
    public String getText(Formatter formatter) {
        return "return " + mExpression.getText() + ";";
    }
}
