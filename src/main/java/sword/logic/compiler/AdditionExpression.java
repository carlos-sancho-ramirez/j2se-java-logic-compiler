package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class AdditionExpression implements Expression {
    private final Type mResultingType;
    private final Expression mLeft;
    private final Expression mRight;

    public AdditionExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.resultingType() instanceof IntegerType);
        ensureValidArguments(right.resultingType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType leftType = (IntegerType) left.resultingType();
        final IntegerType rightType = (IntegerType) right.resultingType();
        final String leftMinText = leftType.getMin().getText();
        final String leftMaxText = leftType.getMax().getText();
        final String rightMinText = rightType.getMin().getText();
        final String rightMaxText = rightType.getMax().getText();
        final boolean leftMinUnbound = leftMinText.equals(ExpressionConstants.unboundText);
        final boolean leftMaxUnbound = leftMaxText.equals(ExpressionConstants.unboundText);
        final boolean rightMinUnbound = rightMinText.equals(ExpressionConstants.unboundText);
        final boolean rightMaxUnbound = rightMaxText.equals(ExpressionConstants.unboundText);
        if ((leftMinUnbound || rightMinUnbound) && (leftMaxUnbound || rightMaxUnbound)) {
            mResultingType = ExpressionConstants.unboundIntegerType;
        }
        else if (leftMinUnbound || rightMinUnbound) {
            mResultingType = new IntegerType(
                    ExpressionConstants.unboundToken,
                    new Token(IntegerLiteralOperations.sum(leftMaxText, rightMaxText)));
        }
        else {
            final Token resultingMinBound = new Token(IntegerLiteralOperations.sum(leftMinText, rightMinText));
            final Token resultingMaxBound = (leftMaxUnbound || rightMaxUnbound)? ExpressionConstants.unboundToken :
                    new Token(IntegerLiteralOperations.sum(leftMaxText, rightMaxText));

            mResultingType = new IntegerType(resultingMinBound, resultingMaxBound);
        }
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

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }
}
