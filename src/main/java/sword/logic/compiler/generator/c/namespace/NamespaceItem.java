package sword.logic.compiler.generator.c.namespace;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class NamespaceItem implements Namespace {
    private final Namespace mParent;
    private final String mLast;

    NamespaceItem(Namespace parent, String item) {
        // TODO: Check if item is valid
        ensureNonNull(parent, item);
        mParent = parent;
        mLast = item;
    }

    public Namespace getParent() {
        return mParent;
    }

    public String last() {
        return mLast;
    }

    @Override
    public NamespaceItem append(String last) {
        return new NamespaceItem(this, last);
    }

    public String symbolText() {
        return (mParent instanceof NamespaceItem parentItem)?
                parentItem.symbolText() + "_" + mLast : mLast;
    }

    @Override
    public int hashCode() {
        return mLast.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof NamespaceItem that &&
                mParent.equals(that.mParent) &&
                mLast.equals(that.mLast);
    }

    @Override
    public String toString() {
        return "Namespace(" + symbolText() + ")";
    }
}
