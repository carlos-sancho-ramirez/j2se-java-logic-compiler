package sword.logic.syntax_tree.statements;

import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class TypeAliasStatement implements Statement {
    private final Token mName;
    private final Type mType;

    public TypeAliasStatement(Token name, Type type) {
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
