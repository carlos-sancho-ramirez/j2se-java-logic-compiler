package sword.collections;

public final class MutableSetExtensions {
    public static <T> void retain(MutableSet<T> set, Predicate<T> predicate) {
        for (T value : set.toImmutable()) {
            if (!predicate.apply(value)) {
                set.remove(value);
            }
        }
    }

    public static <T> void retainNot(MutableSet<T> set, Predicate<T> predicate) {
        for (T value : set.toImmutable()) {
            if (predicate.apply(value)) {
                set.remove(value);
            }
        }
    }

    private MutableSetExtensions() {
    }
}
