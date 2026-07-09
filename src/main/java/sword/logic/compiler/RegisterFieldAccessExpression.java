package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class RegisterFieldAccessExpression implements Expression {
    private final Expression mRegister;
    private final Token mFieldName;

    public RegisterFieldAccessExpression(Expression register, Token fieldName) {
        ensureNonNull(register, fieldName);
        mRegister = register;
        mFieldName = fieldName;
    }

    public Expression getRegister() {
        return mRegister;
    }

    public Token getFieldName() {
        return mFieldName;
    }
}
