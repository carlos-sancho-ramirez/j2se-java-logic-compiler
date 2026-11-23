package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaStringLiteralExpression implements JavaExpression {
    private final String mLiteral;

    public JavaStringLiteralExpression(String literal) {
        ensureValidArguments(literal != null && literal.length() >= 2 && literal.charAt(0) == '"' && literal.charAt(literal.length() - 1) == '"');
        mLiteral = literal;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mLiteral;
    }
}
