package sword.logic.compiler.generator.c.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CStructDeclarationType implements CTypeDeclaration {
    private final String mName;

    public CStructDeclarationType(String name) {
        ensureNonNull(name);
        mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String getText() {
        return "struct " + mName;
    }
}
