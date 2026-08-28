package sword.logic.interpreter.statements;

import sword.collections.MutableMap;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.type.definitions.TypeDefinition;
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

    @Override
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        // Nothing to be done
    }
}
