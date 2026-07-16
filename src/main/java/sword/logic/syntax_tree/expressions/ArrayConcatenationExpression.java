package sword.logic.syntax_tree.expressions;

import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;

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
        final IntegerType leftLengthType = leftType.getLengthType();
        final ArrayType rightType = (ArrayType) right.resultingType();
        final IntegerType rightLengthType = rightType.getLengthType();
        final String resultLengthMinText = IntegerLiteralOperations.sum(leftLengthType.getMin().getText(), rightLengthType.getMin().getText());

        final String leftLengthMaxText = leftType.getLengthType().getMax().getText();
        final String rightLengthMaxText = rightType.getLengthType().getMax().getText();
        final String resultLengthMaxText = (leftLengthMaxText == TypeConstants.unboundText || rightLengthMaxText == TypeConstants.unboundText)? TypeConstants.unboundText : IntegerLiteralOperations.sum(leftLengthMaxText, rightLengthMaxText);
        final Token resultLengthMin = (resultLengthMinText == TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(resultLengthMinText);
        final Token resultLengthMax = (resultLengthMaxText == TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(resultLengthMaxText);
        final IntegerType resultLengthType = new IntegerType(resultLengthMin, resultLengthMax);

        final boolean leftItemIsInteger = leftType.getItemType() instanceof IntegerType;
        final boolean leftItemIsArray = leftType.getItemType() instanceof ArrayType;
        final boolean rightItemIsInteger = rightType.getItemType() instanceof IntegerType;
        final boolean rightItemIsArray = rightType.getItemType() instanceof ArrayType;
        if (leftItemIsInteger && rightItemIsInteger) {
            final IntegerType leftItemType = (IntegerType) leftType.getItemType();
            final IntegerType rightItemType = (IntegerType) rightType.getItemType();
            final IntegerType resultingItemType = leftItemType.getUnion(rightItemType);
            mResultingType = (resultingItemType == leftItemType && leftLengthType.equals(resultLengthType))? leftType :
                    (resultingItemType == rightItemType && rightLengthType.equals(resultLengthType))? rightType : new ArrayType(resultLengthType, resultingItemType);
        }
        else if (leftItemIsInteger && !rightItemIsArray || rightItemIsInteger && !leftItemIsArray) {
            mResultingType = new ArrayType(resultLengthType, TypeConstants.unboundIntegerType);
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
