package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;
import sword.logic.compiler.generator.java.expressions.JavaExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaReturnStatement implements JavaStatement {
    private final JavaExpression mExpression;

    public JavaReturnStatement(JavaExpression expression) {
        ensureNonNull(expression);
        mExpression = expression;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences, Formatter formatter) {
        return formatter.getIndentation() + "return " + mExpression.getText(importedClassReferences) + ";";
    }
}
