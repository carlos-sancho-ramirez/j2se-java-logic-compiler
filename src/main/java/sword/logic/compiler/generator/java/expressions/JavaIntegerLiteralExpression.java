package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

import static sword.logic.compiler.IntegerLiteralOperations.validIntegerLiteral;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaIntegerLiteralExpression implements JavaExpression {
    private final String mLiteral;

    public JavaIntegerLiteralExpression(String literal) {
        ensureValidArguments(validIntegerLiteral(literal));
        mLiteral = literal;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return mLiteral;
    }
}
