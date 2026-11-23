package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaBooleanType implements JavaType {
    private static JavaBooleanType instance;

    public static JavaBooleanType getInstance() {
        if (instance == null) {
            instance = new JavaBooleanType();
        }

        return instance;
    }

    private JavaBooleanType() {
    }

    @Override
    public String getTypeText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "boolean";
    }
}
