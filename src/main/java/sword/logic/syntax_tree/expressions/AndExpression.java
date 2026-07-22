package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class AndExpression implements Expression {
    private final Expression mLeft;
    private final Expression mRight;

    public AndExpression(Expression left, Expression right) {
        ensureNonNull(left, right);
        ensureValidArguments(left.requiredType() == TypeConstants.booleanType);
        ensureValidArguments(right.requiredType() == TypeConstants.booleanType);
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
    public Type requiredType() {
        return TypeConstants.booleanType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == TypeConstants.booleanType) {
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
