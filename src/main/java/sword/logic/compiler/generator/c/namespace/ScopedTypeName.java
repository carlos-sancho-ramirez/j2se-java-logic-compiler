package sword.logic.compiler.generator.c.namespace;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;

public final class ScopedTypeName {
    private final Namespace mNamespace;
    private final String mTypeName;

    ScopedTypeName(Namespace namespace, String typeName) {
        ensureNonNull(namespace);
        ensureValidArguments(validTypeName(typeName));
        mNamespace = namespace;
        mTypeName = typeName;
    }

    public String getSimpleName() {
        return mTypeName;
    }

    @Override
    public int hashCode() {
        return mTypeName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ScopedTypeName that &&
                mNamespace.equals(that.mNamespace) &&
                mTypeName.equals(that.mTypeName);
    }

    public String symbolText() {
        return ((mNamespace instanceof NameSubspace parent)? parent.symbolText() + "_" : "") + mTypeName;
    }

    @Override
    public String toString() {
        return "ScopedTypeName(" + symbolText() + ")";
    }
}
