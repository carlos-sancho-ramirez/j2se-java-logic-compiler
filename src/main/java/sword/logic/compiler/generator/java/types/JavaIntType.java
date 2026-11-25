package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaIntType implements JavaType {
    private static JavaIntType instance;

    public static JavaIntType getInstance() {
        if (instance == null) {
            instance = new JavaIntType();
        }

        return instance;
    }

    private JavaIntType() {
    }

    @Override
    public String getTypeText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "int";
    }

    @Override
    public String getSignatureType() {
        return "I";
    }
}
