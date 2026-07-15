package sword.logic.syntax_tree.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayType implements Type {
    private final Type mItemType;

    public ArrayType(Type itemType) {
        ensureNonNull(itemType);
        mItemType = itemType;
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
