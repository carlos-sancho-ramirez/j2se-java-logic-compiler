package sword.logic.interpreter.expressions;

import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.interpreter.statements.ConstantDefinitionStatement.validConstantName;

public final class RegisterFieldAccessExpression implements Expression {
    private final Token mDotOperator;
    private final Expression mRegister;
    private final Token mFieldName;

    public RegisterFieldAccessExpression(Token dotOperator, Expression register, Token fieldName) {
        ensureValidArguments(dotOperator.getText().equals("."));
        ensureNonNull(register);
        ensureValidArguments(validConstantName(fieldName.getText()));
        mDotOperator = dotOperator;
        mRegister = register;
        mFieldName = fieldName;
    }
}
