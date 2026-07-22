package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class RegisterFieldAccessExpression implements Expression {
    private final Type mFieldType;
    private final Expression mRegister;
    private final Token mFieldName;

    public RegisterFieldAccessExpression(Type fieldType, Expression register, Token fieldName) {
        ensureNonNull(fieldType, register, fieldName);
        mFieldType = fieldType;
        mRegister = register;
        mFieldName = fieldName;
    }

    public Expression getRegister() {
        return mRegister;
    }

    public Token getFieldName() {
        return mFieldName;
    }

    @Override
    public Type resultingType() {
        return mFieldType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (mFieldType == UnknownType.getInstance()) {
            return new RegisterFieldAccessExpression(type, mRegister, mFieldName);
        }
        else if (mFieldType.equals(type)) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already a " + mFieldType.getClass().getSimpleName());
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mRegister.resolveReferences(knownTargets);
    }
}
