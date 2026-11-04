package sword.logic.compiler.generator.c.namespace;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class NameSubspace extends AbstractNamespace {
    private static boolean validNumberForAnonymous(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        else if (value.charAt(0) == '0') {
            return value.length() == 1;
        }
        else if (value.charAt(0) >= '1' && value.charAt(0) <= '9') {
            final int length = value.length();
            for (int i = 1; i < length; i++) {
                final char ch = value.charAt(i);
                if (ch < '0' || ch > '9') {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    private final Namespace mParent;
    private final String mLast;

    NameSubspace(Namespace parent, String item) {
        ensureNonNull(parent, item);
        ensureValidArguments(validConstantName(item) || validNumberForAnonymous(item));
        mParent = parent;
        mLast = item;
    }

    public Namespace getParent() {
        return mParent;
    }

    public String last() {
        return mLast;
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
