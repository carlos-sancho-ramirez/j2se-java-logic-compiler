package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.collections.Procedure;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class MultiplicationExpression implements Expression {
    private final IntegerType mRequiredType;
    private final Expression mLeft;
    private final Expression mRight;

    public MultiplicationExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.requiredType() instanceof IntegerType);
        ensureValidArguments(right.requiredType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType leftType = (IntegerType) left.requiredType();
        final IntegerType rightType = (IntegerType) right.requiredType();
        final String leftMin = leftType.getMin().getText();
        final boolean leftMinIsNegative = leftMin.charAt(0) == '-';
        final String leftMax = leftType.getMax().getText();
        final boolean leftMaxIsNegative = leftMax.charAt(0) == '-';
        final String rightMin = rightType.getMin().getText();
        final boolean rightMinIsNegative = rightMin.charAt(0) == '-';
        final String rightMax = rightType.getMax().getText();
        final boolean rightMaxIsNegative = rightMax.charAt(0) == '-';

        // TODO: We should improve this logic when unbound integers are present. We should be able to delimit this further.
        if (leftMin.equals(TypeConstants.unboundText) || leftMax.equals(TypeConstants.unboundText) ||
                rightMin.equals(TypeConstants.unboundText) || rightMax.equals(TypeConstants.unboundText)) {
            mRequiredType = TypeConstants.unboundIntegerType;
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

            mRequiredType = new IntegerType(new Token(newMin), new Token(newMax));
        }
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    @Override
    public Type requiredType() {
        return mRequiredType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type instanceof IntegerType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already an Integer");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mLeft.resolveReferences(knownTargets);
        mRight.resolveReferences(knownTargets);
    }

    @Override
    public Type resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        final IntegerType leftType = (IntegerType) mLeft.resultingType(paramTypes, logger);
        final IntegerType rightType = (IntegerType) mRight.resultingType(paramTypes, logger);
        final String leftMin = leftType.getMin().getText();
        final boolean leftMinIsNegative = leftMin.charAt(0) == '-';
        final String leftMax = leftType.getMax().getText();
        final boolean leftMaxIsNegative = leftMax.charAt(0) == '-';
        final String rightMin = rightType.getMin().getText();
        final boolean rightMinIsNegative = rightMin.charAt(0) == '-';
        final String rightMax = rightType.getMax().getText();
        final boolean rightMaxIsNegative = rightMax.charAt(0) == '-';

        // TODO: We should improve this logic when unbound integers are present. We should be able to delimit this further.
        if (leftMin.equals(TypeConstants.unboundText) || leftMax.equals(TypeConstants.unboundText) ||
                rightMin.equals(TypeConstants.unboundText) || rightMax.equals(TypeConstants.unboundText)) {
            return TypeConstants.unboundIntegerType;
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

            return new IntegerType(new Token(newMin), new Token(newMax));
        }
    }
}
