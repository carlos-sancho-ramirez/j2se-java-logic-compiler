package sword.logic.interpreter.type.definitions;

import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class FunctionParameter {
    private final Token mName;
    private final TypeMention mType;

    public FunctionParameter(Token name, TypeMention type) {
        ensureValidArguments(validConstantName(name.getText()));
        ensureNonNull(type);
        mName = name;
        mType = type;
    }

    public Token getName() {
        return mName;
    }

    public TypeMention getType() {
        return mType;
    }

    public sword.logic.types.FunctionParameter untokenize(TypeAliasResolver resolver) {
        try {
            return new sword.logic.types.FunctionParameter(mName.getText(), mType.resolve(resolver));
        }
        catch (UnresolvedTypeReferenceException e) {
            throw new RuntimeException("Unable to untokenize FunctionParameter with type " + mType, e);
        }
    }
}
