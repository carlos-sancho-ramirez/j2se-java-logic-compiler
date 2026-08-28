package sword.logic.interpreter.type.definitions;

import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;

public final class ReferenceTypeDefinition implements TypeMention {
    private final Token mTypeAlias;

    public ReferenceTypeDefinition(Token typeAlias) {
        ensureValidArguments(validTypeName(typeAlias.getText()));
        mTypeAlias = typeAlias;
    }

    public Token getTypeAlias() {
        return mTypeAlias;
    }

    @Override
    public Type resolve(TypeAliasResolver resolver) throws UnresolvedTypeReferenceException {
        return resolver.resolveTypeAlias(mTypeAlias);
    }

    @Override
    public Type untokenize() {
        return null;
    }
}
