package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaShortType implements JavaType {
    private static JavaShortType instance;

    public static JavaShortType getInstance() {
        if (instance == null) {
            instance = new JavaShortType();
        }

        return instance;
    }

    private JavaShortType() {
    }

    @Override
    public String getTypeText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "short";
    }
}
