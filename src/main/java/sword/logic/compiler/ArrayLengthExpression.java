package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayLengthExpression implements Expression {
    private final IntegerType mResultingType;
    private final Expression mArray;

    public ArrayLengthExpression(Expression array) {
        ensureNonNull(array);
        mArray = array;
        mResultingType = new IntegerType(new Token("0"), ExpressionConstants.unboundToken);
    }

    public Expression getArray() {
        return mArray;
    }

    @Override
    public Type resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type instanceof IntegerType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already an Integer");
        }
    }
}
