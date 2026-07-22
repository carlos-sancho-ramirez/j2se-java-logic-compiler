package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
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

public final class SubtractionExpression implements Expression {
    private final IntegerType mResultingType;
    private final Expression mLeft;
    private final Expression mRight;

    public SubtractionExpression(Expression left, Expression right) {
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
        final boolean leftMinUnbound = leftMinText.equals(TypeConstants.unboundText);
        final boolean leftMaxUnbound = leftMaxText.equals(TypeConstants.unboundText);
        final boolean rightMinUnbound = rightMinText.equals(TypeConstants.unboundText);
        final boolean rightMaxUnbound = rightMaxText.equals(TypeConstants.unboundText);

        // TODO: Improve this logic to delimit integer ranges
        if (leftMinUnbound || leftMaxUnbound || rightMinUnbound || rightMaxUnbound) {
            mResultingType = TypeConstants.unboundIntegerType;
        }
        else {
            mResultingType = new IntegerType(
                    new Token(IntegerLiteralOperations.subtraction(leftMinText, rightMaxText)),
                    new Token(IntegerLiteralOperations.subtraction(leftMaxText, rightMinText)));
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

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mLeft.resolveReferences(knownTargets);
        mRight.resolveReferences(knownTargets);
    }
}
