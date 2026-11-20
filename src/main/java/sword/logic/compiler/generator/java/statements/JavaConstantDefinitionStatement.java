package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaVariable;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;
import sword.logic.compiler.generator.java.expressions.JavaExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaConstantDefinitionStatement implements JavaStatement {
    private final JavaVariable mTarget;
    private final JavaExpression mExpression;

    public JavaConstantDefinitionStatement(
            JavaVariable target,
            JavaExpression expression) {
        ensureNonNull(target, expression);
        mTarget = target;
        mExpression = expression;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, Formatter formatter) {
        return formatter.getIndentation() + "final " + mTarget.getType().getTypeText(importedClassReferences) + " " + mTarget.getName() + " = " + mExpression.getText(importedClassReferences) + ";";
    }
}
