package sword.logic.compiler.generator.c;

import sword.logic.compiler.generator.c.types.CTypeDeclaration;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CVariable {
    private final String mName;
    private final CTypeDeclaration mType;

    public CVariable(String name, CTypeDeclaration type) {
        ensureNonNull(name, type);
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public CTypeDeclaration getTypeDeclaration() {
        return mType;
    }
}
