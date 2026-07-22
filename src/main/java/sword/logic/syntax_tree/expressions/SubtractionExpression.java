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

public final class SubtractionExpression implements Expression {
    private final IntegerType mRequiredType;
    private final Expression mLeft;
    private final Expression mRight;

    public SubtractionExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.requiredType() instanceof IntegerType);
        ensureValidArguments(right.requiredType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType leftType = (IntegerType) left.requiredType();
        final IntegerType rightType = (IntegerType) right.requiredType();
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
            mRequiredType = TypeConstants.unboundIntegerType;
        }
        else {
            mRequiredType = new IntegerType(
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
    public IntegerType resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        final IntegerType leftType = (IntegerType) mLeft.resultingType(paramTypes, logger);
        final IntegerType rightType = (IntegerType) mRight.resultingType(paramTypes, logger);
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
            return TypeConstants.unboundIntegerType;
        }
        else {
            return new IntegerType(
                    new Token(IntegerLiteralOperations.subtraction(leftMinText, rightMaxText)),
                    new Token(IntegerLiteralOperations.subtraction(leftMaxText, rightMinText)));
        }
    }
}
