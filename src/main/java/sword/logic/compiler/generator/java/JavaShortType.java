package sword.logic.compiler.generator.java;

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
}
