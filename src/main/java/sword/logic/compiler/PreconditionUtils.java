package sword.logic.compiler;

public final class PreconditionUtils {
    public static void ensureNonNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    public static void ensureValidArguments(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }
}
