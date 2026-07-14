package sword.logic.compiler;

public final class UnknownType implements Type {
    private static final UnknownType mInstance = new UnknownType();

    public static UnknownType getInstance() {
        return mInstance;
    }

    private UnknownType() {
    }
}
