package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class DivisionExpression implements Expression {
    private final IntegerType mRequiredType;
    private final Expression mLeft;
    private final Expression mRight;

    public DivisionExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.requiredType() instanceof IntegerType);
        ensureValidArguments(right.requiredType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType leftType = (IntegerType) left.requiredType();
        final IntegerType rightType = (IntegerType) right.requiredType();
        final String leftMin = leftType.getMin().getText();
        final String leftMax = leftType.getMax().getText();
        final String rightMin = rightType.getMin().getText();
        final String rightMax = rightType.getMax().getText();

        final String newMin;
        final String newMax;
        if (leftMin.equals("*") || leftMax.equals("*") || rightMin.equals("*") || rightMax.equals("*")) {
            newMin = "*";
            newMax = "*";
        }
        else {
            final boolean leftMinIsNegative = leftMin.charAt(0) == '-';
            final boolean leftMaxIsNegative = leftMax.charAt(0) == '-';
            final boolean rightMinIsNegative = rightMin.charAt(0) == '-';
            final boolean rightMaxIsNegative = rightMax.charAt(0) == '-';

            if (leftMaxIsNegative) {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.division(leftMax, rightMin);
                    newMax = IntegerLiteralOperations.division(leftMin, rightMax);
                }
                else if (rightMinIsNegative) {
                    newMin = leftMin;
                    newMax = rightMin;
                }
                else {
                    newMin = IntegerLiteralOperations.division(leftMin, rightMin);
                    newMax = IntegerLiteralOperations.division(leftMax, rightMax);
                }
            }
            else if (leftMinIsNegative) {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.division(leftMax, rightMax);
                    newMax = IntegerLiteralOperations.division(leftMin, rightMax);
                }
                else if (rightMinIsNegative) {
                    newMin = IntegerLiteralOperations.min(leftMin, "-" + leftMax);
                    newMax = IntegerLiteralOperations.max("-" + leftMin, leftMax);
                }
                else {
                    newMin = IntegerLiteralOperations.division(leftMin, rightMin);
                    newMax = IntegerLiteralOperations.division(leftMax, rightMin);
                }
            }
            else {
                if (rightMaxIsNegative) {
                    newMin = IntegerLiteralOperations.division(leftMax, rightMax);
                    newMax = IntegerLiteralOperations.division(leftMin, rightMax);
                }
                else if (rightMinIsNegative) {
                    newMin = "-" + leftMax;
                    newMax = leftMax;
                }
                else {
                    newMin = IntegerLiteralOperations.division(leftMin, rightMax);
                    newMax = IntegerLiteralOperations.division(leftMax, rightMin);
                }
            }
        }

        mRequiredType = new IntegerType(new Token(newMin), new Token(newMax));
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    @Override
    public IntegerType requiredType() {
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
}
