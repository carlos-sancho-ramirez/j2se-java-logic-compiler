package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

public final class JavaVoidType implements JavaVoidableType {
    private static JavaVoidType instance;

    public static JavaVoidType getInstance() {
        if (instance == null) {
            instance = new JavaVoidType();
        }

        return instance;
    }

    private JavaVoidType() {
    }

    @Override
    public String getTypeText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return "void";
    }
}
