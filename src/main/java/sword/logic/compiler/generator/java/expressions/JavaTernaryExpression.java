package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaTernaryExpression implements JavaExpression {
    private final JavaExpression mCondition;
    private final JavaExpression mThenClause;
    private final JavaExpression mElseClause;

    public JavaTernaryExpression(JavaExpression condition, JavaExpression thenClause, JavaExpression elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        // TODO: Adjust parentheses
        return "(" + mCondition.getText(importedClassReferences) + ")? " + mThenClause.getText(importedClassReferences) + " : " + mElseClause.getText(importedClassReferences);
    }
}
