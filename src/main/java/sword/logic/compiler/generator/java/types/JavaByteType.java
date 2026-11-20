package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

public final class JavaByteType implements JavaType {
    private static JavaByteType instance;

    public static JavaByteType getInstance() {
        if (instance == null) {
            instance = new JavaByteType();
        }

        return instance;
    }

    private JavaByteType() {
    }

    @Override
    public String getTypeText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return "byte";
    }
}
