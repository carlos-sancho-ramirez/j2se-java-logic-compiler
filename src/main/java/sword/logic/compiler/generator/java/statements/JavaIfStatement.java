package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;
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

    private String getTextNoFirstIndentation(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, Formatter formatter) {
        String result = "if (" + mCondition.getText(importedClassReferences) + ") {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (JavaStatement st : mThenStatements) {
            result += st.getText(importedClassReferences, innerFormatter) + "\n";
        }

        if (!mElseStatements.isEmpty()) {
            if (mElseStatements.size() == 1 && mElseStatements.valueAt(0) instanceof JavaIfStatement innerIfStatement) {
                return result +
                        formatter.getIndentation() + "}\n" +
                        formatter.getIndentation() + "else " +
                        innerIfStatement.getTextNoFirstIndentation(importedClassReferences, formatter);
            }
            else {
                result += formatter.getIndentation() + "}\n" +
                        formatter.getIndentation() + "else {\n";
                for (JavaStatement st : mElseStatements) {
                    result += st.getText(importedClassReferences, innerFormatter) + "\n";
                }
            }
        }

        return result + formatter.getIndentation() + "}";
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, Formatter formatter) {
        return formatter.getIndentation() + getTextNoFirstIndentation(importedClassReferences, formatter);
    }
}
