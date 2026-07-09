package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionParameter {
    private final Token mName;
    private final Type mType;

    public FunctionParameter(Token name, Type type) {
        ensureNonNull(name, type);
        mName = name;
        mType = type;
    }

    public Token getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }
}
