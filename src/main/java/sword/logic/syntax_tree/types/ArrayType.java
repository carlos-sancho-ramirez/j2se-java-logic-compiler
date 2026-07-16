package sword.logic.syntax_tree.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayType implements Type {
    private final IntegerType mLengthType;
    private final Type mItemType;

    public ArrayType(IntegerType lengthType, Type itemType) {
        ensureNonNull(lengthType, itemType);
        ensureValidArguments(!lengthType.getMin().getText().equals(TypeConstants.unboundText) && !lengthType.getMin().getText().startsWith("-"));
        mLengthType = lengthType;
        mItemType = itemType;
    }

    public IntegerType getLengthType() {
        return mLengthType;
    }

    public Type getItemType() {
        return mItemType;
    }

    @Override
    public int hashCode() {
        return mItemType.hashCode() + 1;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ArrayType that &&
                mItemType.equals(that.mItemType);
    }
}
