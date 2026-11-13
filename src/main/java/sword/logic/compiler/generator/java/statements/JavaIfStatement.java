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

    @Override
    public String getText(Formatter formatter) {
        String result = formatter.getIndentation() + "if (" + mCondition.getText() + ") {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (JavaStatement st : mThenStatements) {
            result += st.getText(innerFormatter) + "\n";
        }

        if (!mElseStatements.isEmpty()) {
            result += formatter.getIndentation() + "}\n" +
                    formatter.getIndentation() + "else {\n";
            for (JavaStatement st : mElseStatements) {
                result += st.getText(innerFormatter) + "\n";
            }
        }

        return result + formatter.getIndentation() + "}";
    }
}
