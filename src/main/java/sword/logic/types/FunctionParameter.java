package sword.logic.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class FunctionParameter {
    private final String mName;

    // This cannot be final to allow circular dependencies
    private Type mType;

    public FunctionParameter(String name, Type type) {
        ensureValidArguments(validConstantName(name));
        ensureNonNull(type);
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof FunctionParameter that &&
                mName.equals(that.mName) &&
                mType.equals(that.mType);
    }
}
