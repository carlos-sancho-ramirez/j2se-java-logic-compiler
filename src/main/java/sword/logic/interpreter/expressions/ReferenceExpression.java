package sword.logic.interpreter.expressions;

import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.interpreter.statements.ConstantDefinitionStatement.validConstantName;

public final class ReferenceExpression implements Expression {
    private final Token mReference;

    public ReferenceExpression(Token reference) {
        ensureValidArguments(validConstantName(reference.getText()));
        mReference = reference;
    }

    public Token getReference() {
        return mReference;
    }
}
