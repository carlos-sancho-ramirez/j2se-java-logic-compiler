package sword.logic.compiler.generator.java;

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
}
