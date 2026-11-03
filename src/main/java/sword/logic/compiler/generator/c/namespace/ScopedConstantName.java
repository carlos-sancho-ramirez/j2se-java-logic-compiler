package sword.logic.compiler.generator.c.namespace;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class ScopedConstantName {
    private final Namespace mNamespace;
    private final String mConstantName;

    ScopedConstantName(Namespace namespace, String constantName) {
        ensureNonNull(namespace);
        ensureValidArguments(validConstantName(constantName));
        mNamespace = namespace;
        mConstantName = constantName;
    }

    public String getSimpleName() {
        return mConstantName;
    }

    @Override
    public int hashCode() {
        return mConstantName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ScopedConstantName that &&
                mNamespace.equals(that.mNamespace) &&
                mConstantName.equals(that.mConstantName);
    }

    public String symbolText() {
        return ((mNamespace instanceof NameSubspace parent)? parent.symbolText() + "_" : "") + mConstantName;
    }

    @Override
    public String toString() {
        return "ScopedConstantName(" + symbolText() + ")";
    }
}
