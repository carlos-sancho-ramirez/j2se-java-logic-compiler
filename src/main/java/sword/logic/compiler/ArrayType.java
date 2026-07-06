package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayType implements Type {
    private final Type mItemType;

    public ArrayType(Type itemType) {
        ensureNonNull(itemType);
        mItemType = itemType;
    }
}
