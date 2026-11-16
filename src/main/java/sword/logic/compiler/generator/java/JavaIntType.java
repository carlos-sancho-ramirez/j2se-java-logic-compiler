package sword.logic.compiler.generator.java;

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
}
