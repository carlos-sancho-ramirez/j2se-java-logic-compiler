package sword.logic.interpreter.expressions;

import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class LeftRightExpression implements Expression {
    private final Token mOperator;
    private final Expression mLeft;
    private final Expression mRight;

    public LeftRightExpression(Token operator, Expression left, Expression right) {
        ensureValidArguments(
                operator.getText().equals("*") ||
                operator.getText().equals("/") ||
                operator.getText().equals("%") ||
                operator.getText().equals("+") ||
                operator.getText().equals("-") ||
                operator.getText().equals("==") ||
                operator.getText().equals("!=") ||
                operator.getText().equals(">=") ||
                operator.getText().equals("<=") ||
                operator.getText().equals(">") ||
                operator.getText().equals("<") ||
                operator.getText().equals("&") ||
                operator.getText().equals("|"));
        ensureNonNull(left, right);
        mOperator = operator;
        mLeft = left;
        mRight = right;
    }
}
