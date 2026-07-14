package sword.collections;

public interface FunctionThrowing<T, R, E extends Exception> {
    R apply(T param) throws E;
}