package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class SimpleType implements Type {
    private final Token mName;

    public SimpleType(Token name) {
        ensureNonNull(name);
        mName = name;
    }
}
