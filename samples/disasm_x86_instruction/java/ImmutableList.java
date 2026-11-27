package com.example;

public final class ImmutableList<T> {
    private final Object[] mValues;

    private ImmutableList(Object[] values) {
        mValues = values;
    }

    public int size() {
        return mValues.length;
    }

    public T valueAt(int index) {
        return (T) mValues[index];
    }

    public static final class Builder<T> {
        private Object[] mValues = new Object[0];

        public Builder<T> append(T item) {
            final Object[] oldValues = mValues;
            mValues = new Object[oldValues.length + 1];

            for (int i = 0; i < oldValues.length; i++) {
                mValues[i] = oldValues[i];
            }

            mValues[oldValues.length] = item;
            return this;
        }

        public ImmutableList<T> build() {
            return new ImmutableList<>(mValues);
        }
    }
}