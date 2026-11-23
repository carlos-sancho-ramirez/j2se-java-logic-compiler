package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaVariable;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaConstantDeclarationStatement implements JavaStatement {
    private final JavaVariable mTarget;

    public JavaConstantDeclarationStatement(JavaVariable target) {
        ensureNonNull(target);
        mTarget = target;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences, Formatter formatter) {
        return formatter.getIndentation() + "final " + mTarget.getType().getTypeText(importedClassReferences) + " " + mTarget.getName() + ";";
    }
}
