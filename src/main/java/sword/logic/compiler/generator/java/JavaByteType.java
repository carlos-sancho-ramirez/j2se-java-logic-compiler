package sword.logic.compiler.generator.java;

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
}
