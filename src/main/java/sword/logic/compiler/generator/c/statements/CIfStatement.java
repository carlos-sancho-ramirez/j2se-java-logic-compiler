package sword.logic.compiler.generator.c.statements;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.expressions.CExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CIfStatement implements CInFunctionStatement {

    private final CExpression mCondition;
    private final ImmutableList<CInFunctionStatement> mThenClause;
    private final ImmutableList<CInFunctionStatement> mElseClause;

    public CIfStatement(CExpression condition, ImmutableList<CInFunctionStatement> thenClause, ImmutableList<CInFunctionStatement> elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }

    @Override
    public String getText(Formatter formatter) {
        String result = formatter.getIndentation() + "if (" + mCondition.getText() + ") {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (CStatement st : mThenClause) {
            result += st.getText(innerFormatter) + "\n";
        }

        if (!mElseClause.isEmpty()) {
            result += formatter.getIndentation() + "}\n" +
                    formatter.getIndentation() + "else {\n";
            for (CStatement st : mElseClause) {
                result += st.getText(innerFormatter) + "\n";
            }
        }

        return result + formatter.getIndentation() + "}";
    }
}
