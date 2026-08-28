package sword.logic.interpreter.statements;

import sword.logic.interpreter.types.TypeDefinition;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class TypeDefinitionStatement implements Statement {
    private final Token mName;
    private final TypeDefinition mDefinition;

    public static boolean validTypeName(String name) {
        return name.charAt(0) >= 'A' && name.charAt(0) <= 'Z';
    }

    public TypeDefinitionStatement(Token name, TypeDefinition definition) {
        ensureNonNull(name, definition);
        ensureValidArguments(validTypeName(name.getText()));

        mName = name;
        mDefinition = definition;
    }

    @Override
    public Token getName() {
        return mName;
    }

    public TypeDefinition getDefinition() {
        return mDefinition;
    }
}
