package sword.logic.compiler.generator.c.namespace;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class NameSubspace extends AbstractNamespace {
    private final Namespace mParent;
    private final String mLast;

    NameSubspace(Namespace parent, String item) {
        ensureNonNull(parent, item);
        ensureValidArguments(validConstantName(item));
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
    public NameSubspace newSubspace(String name) {
        return new NameSubspace(this, name);
    }

    public String symbolText() {
        return (mParent instanceof NameSubspace parentItem)?
                parentItem.symbolText() + "_" + mLast : mLast;
    }

    @Override
    public int hashCode() {
        return mLast.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof NameSubspace that &&
                mParent.equals(that.mParent) &&
                mLast.equals(that.mLast);
    }

    @Override
    public String toString() {
        return "Namespace(" + symbolText() + ")";
    }
}
