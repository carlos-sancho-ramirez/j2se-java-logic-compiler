package sword.logic.statements;

import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class TypeDefinitionStatement implements Statement {
    private final String mName;
    private final Type mType;

    public static boolean validTypeName(String name) {
        return name.charAt(0) >= 'A' && name.charAt(0) <= 'Z';
    }

    public TypeDefinitionStatement(String name, Type type) {
        ensureNonNull(name, type);
        ensureValidArguments(validTypeName(name));

        mName = name;
        mType = type;
    }

    @Override
    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }
}
