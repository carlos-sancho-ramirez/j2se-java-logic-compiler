package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayConcatenationExpression implements Expression {
    private final ArrayType mResultingType;
    private final Expression mLeft;
    private final Expression mRight;

    public ArrayConcatenationExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.resultingType() instanceof ArrayType);
        ensureValidArguments(right.resultingType() instanceof ArrayType);
        mLeft = left;
        mRight = right;

        final ArrayType leftType = (ArrayType) left.resultingType();
        final ArrayType rightType = (ArrayType) right.resultingType();
        final boolean leftItemIsInteger = leftType.getItemType() instanceof IntegerType;
        final boolean leftItemIsArray = leftType.getItemType() instanceof ArrayType;
        final boolean rightItemIsInteger = rightType.getItemType() instanceof IntegerType;
        final boolean rightItemIsArray = rightType.getItemType() instanceof ArrayType;
        if (leftItemIsInteger && rightItemIsInteger) {
            final IntegerType leftItemType = (IntegerType) leftType.getItemType();
            final IntegerType rightItemType = (IntegerType) rightType.getItemType();
            final IntegerType resultingItemType = leftItemType.getUnion(rightItemType);
            mResultingType = (resultingItemType == leftItemType)? leftType :
                    (resultingItemType == rightItemType)? rightType : new ArrayType(resultingItemType);
        }
        else if (leftItemIsInteger && !rightItemIsArray || rightItemIsInteger && !leftItemIsArray) {
            mResultingType = new ArrayType(ExpressionConstants.unboundIntegerType);
        }
        else {
            throw new UnsupportedOperationException("Incompatible types");
        }
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    @Override
    public ArrayType resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        final Expression newLeft = mLeft.resultTo(type);
        final Expression newRight = mRight.resultTo(type);
        return (newLeft == mLeft && newRight == mRight)? this : new ArrayConcatenationExpression(newLeft, newRight);
    }
}
