package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class MultiplicationExpression implements Expression {
    private final IntegerType mResultingType;
    private final Expression mLeft;
    private final Expression mRight;

    public MultiplicationExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.resultingType() instanceof IntegerType);
        ensureValidArguments(right.resultingType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType leftType = (IntegerType) left.resultingType();
        final IntegerType rightType = (IntegerType) right.resultingType();
        final String leftMin = leftType.getMin().getText();
        final boolean leftMinIsNegative = leftMin.charAt(0) == '-';
        final String leftMax = leftType.getMax().getText();
        final boolean leftMaxIsNegative = leftMax.charAt(0) == '-';
        final String rightMin = rightType.getMin().getText();
        final boolean rightMinIsNegative = rightMin.charAt(0) == '-';
        final String rightMax = rightType.getMax().getText();
        final boolean rightMaxIsNegative = rightMax.charAt(0) == '-';

        // TODO: We should improve this logic when unbound integers are present. We should be able to delimit this further.
        if (leftMin.equals(ExpressionConstants.unboundText) || leftMax.equals(ExpressionConstants.unboundText) ||
                rightMin.equals(ExpressionConstants.unboundText) || rightMax.equals(ExpressionConstants.unboundText)) {
            mResultingType = ExpressionConstants.unboundIntegerType;
        }
        else {
            final String newMin;
            final String newMax;

            if (leftMaxIsNegative) {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.multiplication(leftMax, rightMax);
                    newMax = IntegerLiteralOperations.multiplication(leftMin, rightMin);
                }
                else if (rightMinIsNegative) {
                    newMin = IntegerLiteralOperations.multiplication(leftMin, rightMax);
                    newMax = IntegerLiteralOperations.multiplication(leftMin, rightMin);
                }
                else {
                    newMin = IntegerLiteralOperations.multiplication(leftMin, rightMax);
                    newMax = IntegerLiteralOperations.multiplication(leftMax, rightMin);
                }
            }
            else if (leftMinIsNegative) {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.multiplication(leftMax, rightMin);
                    newMax = IntegerLiteralOperations.multiplication(leftMin, rightMin);
                }
                else if (rightMinIsNegative) {
                    newMin = IntegerLiteralOperations.min(
                            IntegerLiteralOperations.multiplication(leftMin, rightMax),
                            IntegerLiteralOperations.multiplication(leftMax, rightMin));
                    newMax = IntegerLiteralOperations.max(
                            IntegerLiteralOperations.multiplication(leftMin, rightMin),
                            IntegerLiteralOperations.multiplication(leftMax, rightMax));
                }
                else {
                    newMin = IntegerLiteralOperations.multiplication(leftMin, rightMax);
                    newMax = IntegerLiteralOperations.multiplication(leftMax, rightMax);
                }
            }
            else {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.multiplication(leftMax, rightMin);
                    newMax = IntegerLiteralOperations.multiplication(leftMin, rightMax);
                }
                else if (rightMinIsNegative) {
                    newMin = IntegerLiteralOperations.multiplication(leftMax, rightMin);
                    newMax = IntegerLiteralOperations.multiplication(leftMax, rightMax);
                }
                else {
                    newMin = IntegerLiteralOperations.multiplication(leftMin, rightMin);
                    newMax = IntegerLiteralOperations.multiplication(leftMax, rightMax);
                }
            }

            mResultingType = new IntegerType(new Token(newMin), new Token(newMax));
        }
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
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
