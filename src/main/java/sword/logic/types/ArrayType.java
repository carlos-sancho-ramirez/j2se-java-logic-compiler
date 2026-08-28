package sword.logic.types;

import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayType implements Type {
    private final IntType mLength;
    private final Type mItemType;

    public ArrayType(IntType length, Type itemType) {
        ensureValidArguments(!length.getRanges().first().getMin().equals(TypeConstants.unboundText) &&
                length.getRanges().first().getMin().charAt(0) != '-' &&
                (!length.getRanges().first().getMin().equals(TypeConstants.zeroText) || !length.getRanges().last().getMax().equals(TypeConstants.zeroText)));
        ensureNonNull(itemType);
        mLength = length;
        mItemType = itemType;
    }

    public IntType getLengthType() {
        return mLength;
    }

    public Type getItemType() {
        return mItemType;
    }

    @Override
    public Type getIntersection(Type type) {
        if (type instanceof ArrayType other) {
            final IntType newLengthType = mLength.getIntersection(other.getLengthType());
            if (newLengthType == null) {
                return null;
            }
            else {
                final Type newItemType;
                if (mItemType instanceof IntType intItemType) {
                    if (other.getItemType() instanceof IntType otherIntItemType) {
                        newItemType = intItemType.getIntersection(otherIntItemType);
                    }
                    else {
                        return null;
                    }
                }
                else if (mItemType instanceof ArrayType arrayItemType) {
                    if (other.getItemType() instanceof ArrayType otherArrayItemType) {
                        newItemType = arrayItemType.getIntersection(otherArrayItemType);
                    }
                    else {
                        return null;
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }

                return (newItemType == null) ? null : new ArrayType(newLengthType, newItemType);
            }
        }
        else if (type == EmptyArrayType.getInstance()) {
            return EmptyArrayType.getInstance();
        }
        else {
            return null;
        }
    }

    @Override
    public Type getUnion(Type other) {
        if (other instanceof ArrayType that) {
            final IntType newLengthType = mLength.getUnion(that.mLength);
            final Type newItemType = mItemType.getUnion(that.mItemType);
            return (newLengthType == mLength && newItemType == mItemType)? this :
                    (newLengthType == that.mLength && newItemType == that.mItemType)? that :
                    new ArrayType(newLengthType, newItemType);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean canFit(Type type) {
        if (type instanceof ArrayType newType && mLength.canFit(newType.getLengthType())) {
            if (mItemType instanceof IntType intItemType) {
                return newType.getItemType() instanceof IntType newItemType &&
                        intItemType.canFit(newItemType);
            }
            else if (mItemType instanceof ArrayType arrayItemType) {
                return newType.getItemType() instanceof ArrayType newItemType && arrayItemType.canFit(newItemType);
            }
            else {
                // TODO: Handle other types when required
                return false;
            }
        }
        else {
            return type instanceof EmptyArrayType;
        }
    }

    @Override
    public int hashCode() {
        return mItemType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ArrayType that &&
                mLength.equals(that.mLength) &&
                mItemType.equals(that.mItemType);
    }
}
