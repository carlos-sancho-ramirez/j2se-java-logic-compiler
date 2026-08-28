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

    public static boolean noneRepeated(ImmutableList<?> list) {
        return list.toSet().size() == list.size();
    }

    /**
     * Copies this list and replaces the value in the given position with the value provided.
     * @param index Position within the list. This must be between 0 (inclusive) and the list size (exclusive).
     * @param value Value to be set within the new list.
     * @return A new list where the value has been replaced in the given position,
     *         or the same instance if the value on that position was already the same value instance.
     * @throws IndexOutOfBoundsException if value for index is negative, or equals or higher than the current list length.
     */
    public static <T> ImmutableList<T> set(ImmutableList<T> list, int index, T value) throws IndexOutOfBoundsException {
        if (list.valueAt(index) == value) {
            return list;
        }
        else {
            final int size = list.size();
            final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < size; i++) {
                if (i == index) {
                    builder.append(value);
                }
                else {
                    builder.append(list.valueAt(i));
                }
            }

            return builder.build();
        }
    }

    /**
     * Inserts the given element in the given position.
     * <p>
     * This method will not replace the existing value in the position but
     * will shift all values with equal or greater index one position in
     * order to create a gap and insert the given value there.
     * <p>
     * The result of executing this method will increase by 1 the size of the list.
     *
     * @param index Index where the value should be inserted.
     *              Valid indexes goes from 0 to the current value returned by {@link ImmutableList#size()} both included.
     * @param value Value to be included
     * @return a new {@link ImmutableList} where the item in the given position has been inserted.
     * @throws java.lang.IndexOutOfBoundsException if index is invalid.
     */
    public static <T> ImmutableList<T> insertAt(ImmutableList<T> list, int index, T value) {
        final int size = list.size();
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }

        final ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < size; i++) {
            if (i == index) {
                builder.append(value);
            }

            builder.append(list.valueAt(i));
        }

        if (index == size) {
            builder.append(value);
        }

        return builder.build();
    }

    private ImmutableListExtensions() {
    }
}
