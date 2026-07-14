package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class EqualThanExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public EqualThanExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    @Override
    public Type resultingType() {
        return ExpressionConstants.booleanType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type == ExpressionConstants.booleanType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already a Boolean");
        }
    }
}
