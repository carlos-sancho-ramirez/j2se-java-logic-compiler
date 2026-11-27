package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaTrueLiteralExpression implements JavaExpression {
    private static JavaTrueLiteralExpression mInstance;

    public static JavaTrueLiteralExpression getInstance() {
        if (mInstance == null) {
            mInstance = new JavaTrueLiteralExpression();
        }

        return mInstance;
    }

    private JavaTrueLiteralExpression() {
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "true";
    }
}
