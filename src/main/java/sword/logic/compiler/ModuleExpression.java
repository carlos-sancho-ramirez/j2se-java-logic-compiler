package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ModuleExpression implements Expression {
    private final IntegerType mResultingType;
    private final Expression mLeft;
    private final Expression mRight;

    public ModuleExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.resultingType() instanceof IntegerType);
        ensureValidArguments(right.resultingType() instanceof IntegerType);
        mLeft = left;
        mRight = right;

        final IntegerType rightType = (IntegerType) right.resultingType();
        ensureValidArguments(IntegerLiteralOperations.greaterThan(rightType.getMin().getText(), "0"));
        final String newMax = IntegerLiteralOperations.subtraction(rightType.getMax().getText(), "1");
        ensureValidArguments(newMax.charAt(0) != '-');
        mResultingType = new IntegerType(new Token("0"), new Token(newMax));
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
