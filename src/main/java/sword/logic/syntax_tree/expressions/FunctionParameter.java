package sword.logic.syntax_tree.expressions;

import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class FunctionParameter implements ReferenceTarget {
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

    @Override
    public Type getType() {
        return mType;
    }
}
