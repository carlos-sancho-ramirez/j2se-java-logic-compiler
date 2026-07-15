package sword.logic.syntax_tree.expressions;

import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IfExpression implements Expression {
    private final Type mResultingType;
    private final Expression mCondition;
    private final Expression mThenClause;
    private final Expression mElseClause;

    private static boolean compatibleTypes(Type a, Type b) {
        if (a == b) {
            return true;
        }
        else if (a instanceof IntegerType) {
            return b instanceof IntegerType;
        }
        else if (a instanceof ArrayType aCasted) {
            return b instanceof ArrayType bCasted && compatibleTypes(aCasted.getItemType(), bCasted.getItemType());
        }
        else {
            return false;
        }
    }

    public IfExpression(Expression condition, Expression thenClause, Expression elseClause) {
        ensureNonNull(condition, thenClause, elseClause);
        ensureValidArguments(condition.resultingType() == TypeConstants.booleanType);
        ensureValidArguments(compatibleTypes(thenClause.resultingType(), elseClause.resultingType()));
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;

        if (thenClause.resultingType() instanceof IntegerType leftType) {
            mResultingType = leftType.getUnion((IntegerType) elseClause.resultingType());
        }
        else {
            mResultingType = thenClause.resultingType();
        }
    }

    public Expression getCondition() {
        return mCondition;
    }

    public Expression getThenClause() {
        return mThenClause;
    }

    public Expression getElseClause() {
        return mElseClause;
    }

    @Override
    public Type resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        final Expression newThenClause = mThenClause.resultTo(type);
        final Expression newElseClause = mElseClause.resultTo(type);
        return (newThenClause == mThenClause && newElseClause == mElseClause)? this :
                new IfExpression(mCondition, newThenClause, newElseClause);
    }
}
