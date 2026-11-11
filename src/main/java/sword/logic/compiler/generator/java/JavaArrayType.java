package sword.logic.compiler.generator.java;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayType implements JavaType {
    private final JavaType mItemType;

    public JavaArrayType(JavaType itemType) {
        ensureNonNull(itemType);
        mItemType = itemType;
    }

    public JavaType getItemType() {
        return mItemType;
    }

    @Override
    public int hashCode() {
        return mItemType.hashCode() + 37;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaArrayType that &&
                mItemType.equals(that.mItemType);
    }
}
