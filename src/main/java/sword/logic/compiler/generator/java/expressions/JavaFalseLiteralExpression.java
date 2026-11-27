package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaFalseLiteralExpression implements JavaExpression {
    private static JavaFalseLiteralExpression mInstance;

    public static JavaFalseLiteralExpression getInstance() {
        if (mInstance == null) {
            mInstance = new JavaFalseLiteralExpression();
        }

        return mInstance;
    }

    private JavaFalseLiteralExpression() {
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "false";
    }
}
