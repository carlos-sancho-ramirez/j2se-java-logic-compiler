package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.expressions.CAssignableExpression;
import sword.logic.compiler.generator.c.expressions.CExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CAdditionStatement implements CInFunctionStatement {
    private final CAssignableExpression mTarget;
    private final CExpression mExpression;

    public CAdditionStatement(CAssignableExpression target, CExpression expression) {
        ensureNonNull(target, expression);
        mTarget = target;
        mExpression = expression;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mTarget.getText() + " += " + mExpression.getText() + ";";
    }
}
