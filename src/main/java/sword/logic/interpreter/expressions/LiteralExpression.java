package sword.logic.interpreter.expressions;

import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class LiteralExpression implements Expression {
    private final Token mLiteral;

    public LiteralExpression(Token literal) {
        // TODO: Check all valid literals
        ensureValidArguments(literal.getText().charAt(0) >= 'A' && literal.getText().charAt(0) <= 'Z' ||
                literal.getText().charAt(0) == '"' && literal.getText().charAt(literal.getText().length() - 1) == '"' ||
                IntegerLiteralOperations.validIntegerLiteral(literal.getText()));
        mLiteral = literal;
    }
}
