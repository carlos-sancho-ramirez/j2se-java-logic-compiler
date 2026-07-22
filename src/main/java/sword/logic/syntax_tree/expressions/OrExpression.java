package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class OrExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public OrExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.resultingType() == TypeConstants.booleanType);
        ensureValidArguments(right.resultingType() == TypeConstants.booleanType);
        mLeft = left;
        mRight = right;
    }

    public Expression getLeftExpression() {
        return mLeft;
    }

    public Expression getRightExpression() {
        return mRight;
    }

    @Override
    public Type resultingType() {
        return TypeConstants.booleanType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type == TypeConstants.booleanType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already a Boolean");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mLeft.resolveReferences(knownTargets);
        mRight.resolveReferences(knownTargets);
    }
}
