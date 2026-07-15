package sword.logic.syntax_tree.types;

import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class IntegerType implements Type {
    private final Token mMin;
    private final Token mMax;

    public IntegerType(Token min, Token max) {
        ensureNonNull(min, max);
        mMin = min;
        mMax = max;
    }

    public Token getMin() {
        return mMin;
    }

    public Token getMax() {
        return mMax;
    }

    public IntegerType getUnion(IntegerType other) {
        final String minText = mMin.getText();
        final boolean minUnbound = minText.equals(TypeConstants.unboundText);
        final String maxText = mMax.getText();
        final boolean maxUnbound = maxText.equals(TypeConstants.unboundText);
        final Token otherMin = other.getMin();
        final Token otherMax = other.getMax();
        final String otherMinText = otherMin.getText();
        final boolean otherMinUnbound = otherMinText.equals(TypeConstants.unboundText);
        final String otherMaxText = otherMax.getText();
        final boolean otherMaxUnbound = otherMaxText.equals(TypeConstants.unboundText);
        if ((minUnbound || !otherMinUnbound && IntegerLiteralOperations.lowerOrEqualThan(minText, otherMinText)) &&
                (maxUnbound || !otherMaxUnbound && IntegerLiteralOperations.greaterOrEqualThan(maxText, otherMaxText))) {
            return this;
        }
        else if ((otherMinUnbound || !minUnbound && IntegerLiteralOperations.lowerOrEqualThan(otherMinText, minText)) &&
                (otherMaxUnbound || !maxUnbound && IntegerLiteralOperations.greaterOrEqualThan(otherMaxText, maxText))) {
            return other;
        }
        else {
            final Token newMin = (minUnbound || otherMinUnbound)? TypeConstants.unboundToken :
                    new Token(IntegerLiteralOperations.min(minText, otherMinText));
            final Token newMax = (maxUnbound || otherMaxUnbound)? TypeConstants.unboundToken :
                    new Token(IntegerLiteralOperations.max(maxText, otherMaxText));
            return new IntegerType(newMin, newMax);
        }
    }

    @Override
    public int hashCode() {
        return mMax.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof IntegerType that &&
                mMin.equals(that.mMin) &&
                mMax.equals(that.mMax);
    }
}
