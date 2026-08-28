package sword.logic.types;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.syntax_tree.types.TypeConstants;

import java.util.Iterator;

import static sword.logic.compiler.IntegerLiteralOperations.greaterOrEqualThan;
import static sword.logic.compiler.IntegerLiteralOperations.greaterThan;
import static sword.logic.compiler.IntegerLiteralOperations.lowerOrEqualThan;
import static sword.logic.compiler.IntegerLiteralOperations.lowerThan;
import static sword.logic.compiler.IntegerLiteralOperations.toDecimal;
import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IntType implements Type {
    private static IntType mAnyIntType;
    private static IntType mAnyNonNegativeIntType;
    private static IntType mAlwaysZeroType;

    public static IntType getAnyIntType() {
        if (mAnyIntType == null) {
            mAnyIntType = new IntType(TypeConstants.unboundText, TypeConstants.unboundText);
        }

        return mAnyIntType;
    }

    public static IntType getAnyNonNegativeIntType() {
        if (mAnyNonNegativeIntType == null) {
            mAnyNonNegativeIntType = new IntType(TypeConstants.zeroText, TypeConstants.unboundText);
        }

        return mAnyNonNegativeIntType;
    }

    public static IntType getAlwaysZeroType() {
        if (mAlwaysZeroType == null) {
            mAlwaysZeroType = new IntType(TypeConstants.zeroText, TypeConstants.zeroText);
        }

        return mAlwaysZeroType;
    }

    private final ImmutableList<Range> mRanges;

    public IntType(ImmutableList<Range> ranges) {
        ensureValidArguments(!ranges.isEmpty() && ranges.indexes().skipLast(1).allMatch(index ->
                lowerThan(ranges.valueAt(index).getMax(), IntegerLiteralOperations.subtraction(ranges.valueAt(index + 1).getMin(), "1"))));
        mRanges = ranges;
    }

    public IntType(String min, String max) {
        this(new ImmutableList.Builder<Range>().append(new Range(min, max)).build());
    }

    public ImmutableList<Range> getRanges() {
        return mRanges;
    }

    public boolean hasUniqueValue() {
        if (mRanges.size() == 1) {
            final Range range = mRanges.valueAt(0);
            return range.getMin().equals(range.getMax());
        }
        else {
            return false;
        }
    }

    public boolean contains(String value) {
        return IntegerLiteralOperations.validIntegerLiteral(value) && mRanges.anyMatch(range ->
                (range.getMin().equals(TypeConstants.unboundText) || IntegerLiteralOperations.greaterOrEqualThan(value, range.getMin())) &&
                (range.getMax().equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(value, range.getMax())));
    }

    public static IntType withUniqueValue(String value) {
        return new IntType(value, value);
    }

    public IntType include(String value) {
        final String decimalValue = toDecimal(value);
        final int rangeCount = mRanges.size();
        for (int rangeIndex = 0; rangeIndex < rangeCount; rangeIndex++) {
            final Range range = mRanges.valueAt(rangeIndex);
            if (range.getMin().equals(TypeConstants.unboundText)) {
                if (range.getMax().equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(decimalValue, range.getMax())) {
                    return this;
                }
                else if (decimalValue.equals(IntegerLiteralOperations.sum(range.getMax(), "1"))) {
                    if (rangeIndex + 1 < rangeCount && mRanges.valueAt(rangeIndex + 1).getMin().equals(IntegerLiteralOperations.sum(decimalValue, "1"))) {
                        return new IntType(ImmutableListExtensions.set(mRanges.removeAt(rangeIndex), rangeIndex, new Range(TypeConstants.unboundText, mRanges.valueAt(rangeIndex + 1).getMax())));
                    }
                    else {
                        return new IntType(ImmutableListExtensions.set(mRanges, rangeIndex, new Range(TypeConstants.unboundText, decimalValue)));
                    }
                }
            }
            else if (IntegerLiteralOperations.lowerThan(decimalValue, range.getMin())) {
                if (IntegerLiteralOperations.lowerThan(decimalValue, IntegerLiteralOperations.subtraction(range.getMin(), "1"))) {
                    return new IntType(ImmutableListExtensions.insertAt(mRanges, rangeIndex, new Range(decimalValue, decimalValue)));
                }
                else {
                    return new IntType(ImmutableListExtensions.set(mRanges, rangeIndex, new Range(decimalValue, range.getMax())));
                }
            }
            else if (range.getMax().equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(decimalValue, range.getMax())) {
                return this;
            }
            else if (decimalValue.equals(IntegerLiteralOperations.sum(range.getMax(), "1"))) {
                if (rangeIndex + 1 < rangeCount && mRanges.valueAt(rangeIndex + 1).getMin().equals(IntegerLiteralOperations.sum(decimalValue, "1"))) {
                    return new IntType(ImmutableListExtensions.set(mRanges.removeAt(rangeIndex), rangeIndex, new Range(range.getMin(), mRanges.valueAt(rangeIndex + 1).getMax())));
                }
                else {
                    return new IntType(ImmutableListExtensions.set(mRanges, rangeIndex, new Range(range.getMin(), decimalValue)));
                }
            }
        }

        return new IntType(mRanges.append(new Range(decimalValue, decimalValue)));
    }

    /**
     * Creates a new IntType where the given value has been excluded from any range including it.
     * <p>
     * If the value is not included in any of the ranges, the same instance will be returned instead.
     * @param value Integer value to check
     * @return A new IntType where the value is excluded, or the same instance if the value is already not included.
     */
    public IntType exclude(String value) {
        final String decimalValue = toDecimal(value);
        final int rangeCount = mRanges.size();
        for (int rangeIndex = 0; rangeIndex < rangeCount; rangeIndex++) {
            final Range range = mRanges.valueAt(rangeIndex);
            if (range.getMin().equals(decimalValue)) {
                if (range.getMax().equals(decimalValue)) {
                    return new IntType(mRanges.removeAt(rangeIndex));
                }
                else {
                    return new IntType(ImmutableListExtensions.set(mRanges, rangeIndex, new Range(IntegerLiteralOperations.sum(decimalValue, "1"), range.getMax())));
                }
            }
            else if (range.getMax().equals(decimalValue)) {
                return new IntType(ImmutableListExtensions.set(mRanges, rangeIndex, new Range(range.getMin(), IntegerLiteralOperations.subtraction(decimalValue, "1"))));
            }
            else {
                if ((range.getMin().equals(TypeConstants.unboundText) || greaterThan(decimalValue, range.getMin())) &&
                        (range.getMax().equals(TypeConstants.unboundText) || lowerThan(decimalValue, range.getMax()))) {
                    final ImmutableList<Range> partial = ImmutableListExtensions.set(mRanges, rangeIndex, new Range(range.getMin(), IntegerLiteralOperations.subtraction(decimalValue, "1")));
                    return new IntType(ImmutableListExtensions.insertAt(partial, rangeIndex + 1, new Range(IntegerLiteralOperations.sum(decimalValue, "1"), range.getMax())));
                }
            }
        }

        return this;
    }

    @Override
    public IntType getUnion(Type other) {
        if (other instanceof IntType that) {
            final ImmutableList<Range> thatRanges = that.getRanges();
            final ImmutableList.Builder<Range> builder = new ImmutableList.Builder<>();
            final int thisRangeCount = mRanges.size();
            final int thatRangeCount = thatRanges.size();
            int thisRangeIndex = 0;
            int thatRangeIndex = 0;

            while (thisRangeIndex < thisRangeCount && thatRangeIndex < thatRangeCount) {
                Range thisRange = mRanges.valueAt(thisRangeIndex);
                Range thatRange = that.getRanges().valueAt(thatRangeIndex);
                String thisMin = thisRange.getMin();
                String thisMax = thisRange.getMax();
                String thatMin = thatRange.getMin();
                String thatMax = thatRange.getMax();

                String newMin;
                String newMax;
                if (thisMin.equals(TypeConstants.unboundText) || !thatMin.equals(TypeConstants.unboundText) && lowerThan(thisMin, thatMin)) {
                    newMin = thisMin;
                    newMax = thisMax;
                    if (++thisRangeIndex < thisRangeCount) {
                        thisRange = mRanges.valueAt(thisRangeIndex);
                        thisMin = thisRange.getMin();
                        thisMax = thisRange.getMax();
                    }
                    else {
                        thisRange = null;
                        thisMin = null;
                        thisMax = null;
                    }
                }
                else {
                    newMin = thatMin;
                    newMax = thatMax;
                    if (++thatRangeIndex < thatRangeCount) {
                        thatRange = thatRanges.valueAt(thatRangeIndex);
                        thatMin = thisRange.getMin();
                        thatMax = thisRange.getMax();
                    }
                    else {
                        thatRange = null;
                        thatMin = null;
                        thatMax = null;
                    }
                }

                if (newMax.equals(TypeConstants.unboundText)) {
                    builder.append(new Range(newMin, newMax));
                    final ImmutableList<Range> newRanges = builder.build();
                    return newRanges.equals(getAnyIntType().getRanges()) ? getAnyIntType() :
                            newRanges.equals(getAnyNonNegativeIntType().getRanges()) ? getAnyNonNegativeIntType() :
                                    new IntType(newRanges);
                }

                while (thisRangeIndex < thisRangeCount && !thisMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(thisMax, newMax)) {
                    if (++thisRangeIndex < thisRangeCount) {
                        thisRange = mRanges.valueAt(thisRangeIndex);
                        thisMin = thisRange.getMin();
                        thisMax = thisRange.getMax();
                    }
                    else {
                        thisRange = null;
                        thisMin = null;
                        thisMax = null;
                    }
                }

                while (thatRangeIndex < thatRangeCount && !thatMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(thatMax, newMax)) {
                    if (++thatRangeIndex < thatRangeCount) {
                        thatRange = thatRanges.valueAt(thatRangeIndex);
                        thatMin = thatRange.getMin();
                        thatMax = thatRange.getMax();
                    }
                    else {
                        thatRange = null;
                        thatMin = null;
                        thatMax = null;
                    }
                }

                boolean newMaxMoved;
                do {
                    newMaxMoved = false;

                    if (thisRange != null) {
                        if (thisMin.equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(thisMin, IntegerLiteralOperations.sum(newMax, "1"))) {
                            newMax = thisMax;
                            newMaxMoved = true;
                            if (++thisRangeIndex < thisRangeCount) {
                                thisRange = mRanges.valueAt(thisRangeIndex);
                                thisMin = thisRange.getMin();
                                thisMax = thisRange.getMax();
                            }
                            else {
                                thisRange = null;
                                thisMin = null;
                                thisMax = null;
                            }
                        }
                    }

                    if (thatRange != null) {
                        if (thatMin.equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(thatMin, IntegerLiteralOperations.sum(newMax, "1"))) {
                            newMax = thatMax;
                            newMaxMoved = true;
                            if (++thatRangeIndex < thatRangeCount) {
                                thatRange = thatRanges.valueAt(thatRangeIndex);
                                thatMin = thatRange.getMin();
                                thatMax = thatRange.getMax();
                            }
                            else {
                                thatRange = null;
                                thatMin = null;
                                thatMax = null;
                            }
                        }
                    }
                }
                while (newMaxMoved);

                builder.append(new Range(newMin, newMax));
            }

            for (; thisRangeIndex < thisRangeCount; thisRangeIndex++) {
                builder.append(mRanges.valueAt(thisRangeIndex));
            }

            for (; thatRangeIndex < thatRangeCount; thatRangeIndex++) {
                builder.append(thatRanges.valueAt(thatRangeIndex));
            }

            final ImmutableList<Range> newRanges = builder.build();
            return newRanges.equals(mRanges) ? this :
                    newRanges.equals(thatRanges) ? that :
                    newRanges.equals(getAnyIntType().getRanges()) ? getAnyIntType() :
                    newRanges.equals(getAnyNonNegativeIntType().getRanges()) ? getAnyNonNegativeIntType() :
                    newRanges.equals(getAlwaysZeroType().getRanges()) ? getAlwaysZeroType() :
                    new IntType(newRanges);
        }
        else {
            return null;
        }
    }

    @Override
    public IntType getIntersection(Type type) {
        if (type instanceof IntType other) {
            final ImmutableList.Builder<Range> builder = new ImmutableList.Builder<>();
            final int thisRangeCount = mRanges.size();
            final int thatRangeCount = other.getRanges().size();
            int thisRangeIndex = 0;
            int thatRangeIndex = 0;

            while (thisRangeIndex < thisRangeCount && thatRangeIndex < thatRangeCount) {
                Range thisRange = mRanges.valueAt(thisRangeIndex);
                Range thatRange = other.getRanges().valueAt(thatRangeIndex);
                String thisRangeMin = thisRange.getMin();
                String thatRangeMin = thatRange.getMin();
                String thisRangeMax = thisRange.getMax();
                String thatRangeMax = thatRange.getMax();
                if (thisRangeMin.equals(TypeConstants.unboundText)) {
                    if (thatRangeMin.equals(TypeConstants.unboundText)) {
                        if (thisRangeMax.equals(TypeConstants.unboundText)) {
                            return other;
                        }
                        else if (thatRangeMax.equals(TypeConstants.unboundText)) {
                            return this;
                        }
                        else if (thisRangeMax.equals(thatRangeMax)) {
                            builder.append(thisRange);
                            thisRangeIndex++;
                            thatRangeIndex++;
                        }
                        else if (lowerThan(thisRangeMax, thatRangeMax)) {
                            builder.append(thisRange);
                            thisRangeIndex++;
                        }
                        else {
                            builder.append(thatRange);
                            thatRangeIndex++;
                        }
                    }
                    else if (thisRangeMax.equals(TypeConstants.unboundText)) {
                        return other;
                    }
                    else if (thatRangeMax.equals(TypeConstants.unboundText)) {
                        if (greaterOrEqualThan(thisRangeMax, thatRangeMin)) {
                            builder.append(new Range(thatRangeMin, thisRangeMax));
                        }
                        thisRangeIndex++;
                    }
                    else if (thisRangeMax.equals(thatRangeMax)) {
                        builder.append(thatRange);
                        thisRangeIndex++;
                        thatRangeIndex++;
                    }
                    else if (lowerThan(thisRangeMax, thatRangeMax)) {
                        builder.append(new Range(thatRangeMin, thisRangeMax));
                        thisRangeIndex++;
                    }
                    else {
                        builder.append(thatRange);
                        thatRangeIndex++;
                    }
                }
                else if (thatRangeMin.equals(TypeConstants.unboundText)) {
                    if (thisRangeMax.equals(TypeConstants.unboundText)) {
                        if (thatRangeMax.equals(TypeConstants.unboundText)) {
                            return this;
                        }
                        else {
                            if (IntegerLiteralOperations.lowerOrEqualThan(thisRangeMin, thatRangeMax)) {
                                builder.append(new Range(thisRangeMin, thatRangeMax));
                            }

                            thatRangeIndex++;
                        }
                    }
                    else if (thatRangeMax.equals(TypeConstants.unboundText)) {
                        return this;
                    }
                    else if (greaterThan(thisRangeMin, thatRangeMax)) {
                        thatRangeIndex++;
                    }
                    else if (IntegerLiteralOperations.lowerOrEqualThan(thisRangeMax, thatRangeMax)) {
                        builder.append(thisRange);
                        thisRangeIndex++;
                    }
                    else {
                        builder.append(new Range(thisRangeMin, thatRangeMax));
                        thatRangeIndex++;
                    }
                }
                else if (thisRangeMax.equals(TypeConstants.unboundText)) {
                    if (thatRangeMax.equals(TypeConstants.unboundText)) {
                        if (greaterOrEqualThan(thisRangeMin, thatRangeMin)) {
                            builder.append(thisRange);
                        }
                        else {
                            builder.append(thatRange);
                        }

                        thisRangeIndex++;
                        thatRangeIndex++;
                    }
                    else {
                        if (greaterOrEqualThan(thatRangeMin, thisRangeMin)) {
                            builder.append(thatRange);
                        }
                        else if (greaterOrEqualThan(thatRangeMax, thisRangeMin)) {
                            builder.append(new Range(thisRangeMin, thatRangeMax));
                        }

                        thatRangeIndex++;
                    }
                }
                else if (thatRangeMax.equals(TypeConstants.unboundText)) {
                    if (greaterOrEqualThan(thisRangeMin, thatRangeMin)) {
                        builder.append(thisRange);
                    }
                    else if (greaterOrEqualThan(thisRangeMax, thatRangeMin)) {
                        builder.append(new Range(thatRangeMin, thisRangeMax));
                    }
                    thisRangeIndex++;
                }
                else {
                    if (greaterThan(thisRangeMin, thatRangeMax)) {
                        thatRangeIndex++;
                    }
                    else if (greaterThan(thisRangeMin, thatRangeMin)) {
                        if (greaterOrEqualThan(thisRangeMax, thatRangeMax)) {
                            builder.append(new Range(thisRangeMin, thatRangeMax));
                            thatRangeIndex++;
                        }
                        else {
                            builder.append(thisRange);
                            thisRangeIndex++;
                        }
                    }
                    else if (thisRangeMin.equals(thatRangeMin)) {
                        if (greaterThan(thatRangeMax, thisRangeMax)) {
                            builder.append(thisRange);
                            thisRangeIndex++;
                        }
                        else {
                            builder.append(thatRange);
                            thatRangeIndex++;
                        }
                    }
                    else if (lowerOrEqualThan(thatRangeMax, thisRangeMax)) {
                        builder.append(thatRange);
                        thatRangeIndex++;
                    }
                    else if (lowerThan(thisRangeMax, thatRangeMin)) {
                        thisRangeIndex++;
                    }
                    else {
                        builder.append(new Range(thatRangeMin, thisRangeMax));
                        thisRangeIndex++;
                    }
                }
            }

            final ImmutableList<Range> newRanges = builder.build();
            return newRanges.isEmpty()? null :
                    newRanges.equals(mRanges)? this :
                    newRanges.equals(other.getRanges())? other :
                    new IntType(newRanges);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean canFit(Type type) {
        return type instanceof IntType newType && newType.equals(getIntersection(newType));
    }

    @Override
    public int hashCode() {
        return mRanges.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof IntType that &&
                mRanges.equals(that.mRanges);
    }

    @Override
    public String toString() {
        return "IntType[" + mRanges.map(range -> {
            final String min = range.getMin();
            final String max = range.getMax();
            return min.equals(max)? min : min + ".." + max;
        }).reduce((a, b) -> a + " | " + b) + "]";
    }

    public static final class Range implements Iterable<String> {
        private final String mMin;
        private final String mMax;

        public Range(String min, String max) {
            ensureValidArguments(min.equals(TypeConstants.unboundText) || max.equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(min, max));
            mMin = min.equals(TypeConstants.unboundText)? TypeConstants.unboundText : toDecimal(min);
            mMax = max.equals(TypeConstants.unboundText)? TypeConstants.unboundText : toDecimal(max);
        }

        public String getMin() {
            return mMin;
        }

        public String getMax() {
            return mMax;
        }

        @Override
        public int hashCode() {
            return mMin.hashCode() ^ mMax.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof Range that &&
                    mMin.equals(that.mMin) &&
                    mMax.equals(that.mMax);
        }

        @Override
        public Iterator<String> iterator() {
            return new RangeIterator(this);
        }
    }

    private static final class RangeIterator implements Iterator<String> {
        private final Range mRange;
        private String mNext;

        RangeIterator(Range range) {
            ensureNonNull(range);
            mRange = range;
            mNext = range.getMin();
        }

        @Override
        public boolean hasNext() {
            return mNext != null;
        }

        @Override
        public String next() {
            final String result = mNext;
            mNext = mRange.getMax().equals(mNext)? null : IntegerLiteralOperations.sum(result, "1");
            return result;
        }
    }
}
