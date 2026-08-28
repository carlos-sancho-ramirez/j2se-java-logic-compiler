package sword.logic.interpreter.types;

import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.interpreter.statements.TypeDefinitionStatement.validTypeName;

public final class ReferenceTypeDefinition implements TypeMention {
    private final Token mType;

    public ReferenceTypeDefinition(Token type) {
        ensureValidArguments(validTypeName(type.getText()));
        mType = type;
    }
}
