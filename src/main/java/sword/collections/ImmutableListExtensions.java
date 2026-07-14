package sword.collections;

public final class ImmutableListExtensions {
    public static <T, U, E extends Exception> ImmutableList<U> mapThrowing(ImmutableList<T> list, FunctionThrowing<? super T, ? extends U, E> func) throws E {
        final int length = list.size();
        boolean somethingChanged = false;
        final Object[] newValues = new Object[length];
        for (int i = 0; i < length; i++) {
            final T value = list.valueAt(i);
            newValues[i] = func.apply(value);
            somethingChanged |= newValues[i] != value;
        }

        return somethingChanged? new ImmutableList<>(newValues) : (ImmutableList<U>) list;
    }

    private ImmutableListExtensions() {
    }
}
