package sword.logic.compiler;

import sword.collections.ImmutableMap;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterType implements Type {
    private final ImmutableMap<Token, Type> mFields;

    public RegisterType(ImmutableMap<Token, Type> fields) {
        ensureValidArguments(!fields.isEmpty());
        mFields = fields;
    }

    public ImmutableMap<Token, Type> getFields() {
        return mFields;
    }
}
