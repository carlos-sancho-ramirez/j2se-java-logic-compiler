package com.example;

public final class ImmutableIntList {
    private final int[] mValues;

    private ImmutableIntList(int[] values) {
        mValues = values;
    }

    public int size() {
        return mValues.length;
    }

    public int valueAt(int index) {
        return mValues[index];
    }

    public static final class Builder {
        private int[] mValues = new int[0];

        public Builder append(int item) {
            final int[] oldValues = mValues;
            mValues = new int[oldValues.length + 1];

            for (int i = 0; i < oldValues.length; i++) {
                mValues[i] = oldValues[i];
            }

            mValues[oldValues.length] = item;
            return this;
        }

        public ImmutableIntList build() {
            return new ImmutableIntList(mValues);
        }
    }
}