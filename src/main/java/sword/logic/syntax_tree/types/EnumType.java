package sword.logic.syntax_tree.types;

import sword.collections.ImmutableSet;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class EnumType implements Type {
    private final Token mName;
    private final ImmutableSet<Token> mPossibleValues;

    private static boolean isValidValue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        final int length = value.length();
        for (int i = 0; i < length; i++) {
            if ((value.charAt(i) < 'A' || value.charAt(i) > 'Z') && value.charAt(i) != '_') {
                return false;
            }
        }

        return true;
    }

    public Token getName() {
        return mName;
    }

    public EnumType(Token name, ImmutableSet<Token> possibleValues) {
        ensureNonNull(name, possibleValues);
        ensureValidArguments(possibleValues.size() >= 2 && possibleValues.allMatch(v -> isValidValue(v.getText())));
        mName = name;
        mPossibleValues = possibleValues;
    }
}
