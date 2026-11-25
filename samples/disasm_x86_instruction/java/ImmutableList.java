package com.example;

public interface ImmutableList<T> {
    int size();
    T valueAt(int index);

    class Builder<T> {
        public Builder append(T item) {
            throw new UnsupportedOperationException("Unimplemented");
        }

        public ImmutableList<T> build() {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }
}