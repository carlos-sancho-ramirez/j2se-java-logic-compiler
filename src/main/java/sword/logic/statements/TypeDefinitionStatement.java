package sword.logic.statements;

import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class TypeDefinitionStatement implements Statement {
    private final String mName;
    private final Type mType;

    public static boolean validTypeName(String name) {
        if (name == null) {
            return false;
        }

        final int length = name.length();
        if (length == 0) {
            return false;
        }

        if (name.charAt(0) < 'A' || name.charAt(0) > 'Z') {
            return false;
        }

        boolean lowerCaseFound = false;
        for (int i = 1; i < length; i++) {
            final char ch = name.charAt(i);
            if (ch >= 'a' && ch <= 'z') {
                lowerCaseFound = true;
            }
            else if ((ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')) {
                return false;
            }
        }

        return lowerCaseFound;
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
