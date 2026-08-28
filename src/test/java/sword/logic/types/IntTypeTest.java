package sword.logic.types;

import org.junit.jupiter.api.Test;
import sword.collections.ImmutableList;
import sword.logic.types.IntType.Range;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public final class IntTypeTest {
    @Test
    void getUnionOnItself() {
        final IntType intType = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("0", "3"))
                .build());
        assertSame(intType, intType.getUnion(intType));
    }

    @Test
    void getUnionOnSingleFittingInside() {
        final IntType intType1 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("0", "3"))
                .build());
        final IntType intType2 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("1", "2"))
                .build());
        assertSame(intType1, intType1.getUnion(intType2));
        assertSame(intType1, intType2.getUnion(intType1));
    }

    @Test
    void getUnionForWhole() {
        final IntType intType = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("-1", "7"))
                .build());
        assertSame(IntType.getAnyIntType(), IntType.getAnyIntType().getUnion(intType));
        assertSame(IntType.getAnyIntType(), intType.getUnion(IntType.getAnyIntType()));
        assertSame(IntType.getAnyIntType(), IntType.getAnyIntType().getUnion(IntType.getAnyIntType()));
    }

    @Test
    void getUnionForMultipleSingleValueRanges() {
        final IntType intType1 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("2", "2"))
                .build());
        final IntType intType2 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("4", "4"))
                .build());
        final IntType intType12 = intType1.getUnion(intType2);
        assertEquals(2, intType12.getRanges().size());
        assertEquals("2", intType12.getRanges().valueAt(0).getMin());
        assertEquals("2", intType12.getRanges().valueAt(0).getMax());
        assertEquals("4", intType12.getRanges().valueAt(1).getMin());
        assertEquals("4", intType12.getRanges().valueAt(1).getMax());

        final IntType intType3 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("5", "5"))
                .build());
        final IntType intType123 = intType12.getUnion(intType3);
        assertEquals(2, intType123.getRanges().size());
        assertEquals("2", intType123.getRanges().valueAt(0).getMin());
        assertEquals("2", intType123.getRanges().valueAt(0).getMax());
        assertEquals("4", intType123.getRanges().valueAt(1).getMin());
        assertEquals("5", intType123.getRanges().valueAt(1).getMax());
        assertEquals(intType123, intType3.getUnion(intType12));
    }

    @Test
    void getIntersection() {
        final IntType intType1 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("0", "5"))
                .append(new Range("7", "255"))
                .build());
        final IntType intType2 = new IntType(new ImmutableList.Builder<Range>()
                .append(new Range("14", "14"))
                .build());
        assertSame(intType2, intType1.getIntersection(intType2));
        assertSame(intType2, intType2.getIntersection(intType1));
    }
}
