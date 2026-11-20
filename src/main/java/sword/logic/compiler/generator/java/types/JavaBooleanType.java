package sword.logic.compiler.generator.java.types;

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
}
