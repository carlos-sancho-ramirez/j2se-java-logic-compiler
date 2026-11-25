package com.example;

public interface ImmutableIntList {
    int size();
    int valueAt(int index);

    class Builder {
        public Builder append(int item) {
            throw new UnsupportedOperationException("Unimplemented");
        }

        public ImmutableIntList build() {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }
}