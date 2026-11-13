package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.expressions.JavaExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaIfStatement implements JavaStatement {
    private final JavaExpression mCondition;
    private final ImmutableList<JavaStatement> mThenStatements;
    private final ImmutableList<JavaStatement> mElseStatements;

    public JavaIfStatement(JavaExpression condition, ImmutableList<JavaStatement> thenStatements, ImmutableList<JavaStatement> elseStatements) {
        ensureNonNull(condition, thenStatements, elseStatements);
        mCondition = condition;
        mThenStatements = thenStatements;
        mElseStatements = elseStatements;
    }

    private String getTextNoFirstIndentation(Formatter formatter) {
        String result = "if (" + mCondition.getText() + ") {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (JavaStatement st : mThenStatements) {
            result += st.getText(innerFormatter) + "\n";
        }

        if (!mElseStatements.isEmpty()) {
            if (mElseStatements.size() == 1 && mElseStatements.valueAt(0) instanceof JavaIfStatement innerIfStatement) {
                return result +
                        formatter.getIndentation() + "}\n" +
                        formatter.getIndentation() + "else " +
                        innerIfStatement.getTextNoFirstIndentation(formatter);
            }
            else {
                result += formatter.getIndentation() + "}\n" +
                        formatter.getIndentation() + "else {\n";
                for (JavaStatement st : mElseStatements) {
                    result += st.getText(innerFormatter) + "\n";
                }
            }
        }

        return result + formatter.getIndentation() + "}";
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + getTextNoFirstIndentation(formatter);
    }
}
