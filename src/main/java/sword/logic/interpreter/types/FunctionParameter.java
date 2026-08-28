package sword.logic.interpreter.types;

import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.interpreter.statements.ConstantDefinitionStatement.validConstantName;

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
}
