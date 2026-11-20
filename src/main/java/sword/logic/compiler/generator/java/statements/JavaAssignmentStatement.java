package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;
import sword.logic.compiler.generator.java.expressions.JavaAssignableExpression;
import sword.logic.compiler.generator.java.expressions.JavaExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaAssignmentStatement implements JavaStatement {
    private final JavaAssignableExpression mTarget;
    private final JavaExpression mExpression;

    public JavaAssignmentStatement(
            JavaAssignableExpression target,
            JavaExpression expression) {
        ensureNonNull(target, expression);
        mTarget = target;
        mExpression = expression;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, Formatter formatter) {
        return formatter.getIndentation() + mTarget.getText(importedClassReferences) + " = " + mExpression.getText(importedClassReferences) + ";";
    }
}
