package sword.logic.expressions;

import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class RegisterFieldAccessExpression implements Expression {
    public static final String ARRAY_FIELD_LENGTH = "length";

    private final Expression mRegister;
    private final String mFieldName;

    public RegisterFieldAccessExpression(Expression register, String fieldName) {
        ensureNonNull(register);
        ensureValidArguments(validConstantName(fieldName));
        mRegister = register;
        mFieldName = fieldName;
    }

    public Expression getRegister() {
        return mRegister;
    }

    public String getFieldName() {
        return mFieldName;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mRegister.dependencies();
    }
}
