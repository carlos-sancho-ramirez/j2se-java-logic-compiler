package sword.logic.syntax_tree.types;

import sword.collections.ImmutableMap;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterType implements Type {
    private final Token mName;
    private final ImmutableMap<Token, Type> mFields;

    public RegisterType(Token name, ImmutableMap<Token, Type> fields) {
        ensureNonNull(name);
        ensureValidArguments(!fields.isEmpty());
        mName = name;
        mFields = fields;
    }

    public Token getName() {
        return mName;
    }

    public ImmutableMap<Token, Type> getFields() {
        return mFields;
    }

    @Override
    public int hashCode() {
        return mFields.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof RegisterType that &&
                mFields.equalMap(that.mFields);
    }
}
