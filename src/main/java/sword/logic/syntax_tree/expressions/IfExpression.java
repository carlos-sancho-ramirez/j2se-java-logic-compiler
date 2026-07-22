package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.Procedure;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.RegisterType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IfExpression implements Expression {
    private final Type mRequiredType;
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
        ensureValidArguments(condition.requiredType() == TypeConstants.booleanType);
        ensureValidArguments(compatibleTypes(thenClause.requiredType(), elseClause.requiredType()));
        mCondition = condition;
        mThenClause = thenClause;
        mElseClause = elseClause;

        if (thenClause.requiredType() instanceof IntegerType leftType) {
            mRequiredType = leftType.getUnion((IntegerType) elseClause.requiredType());
        }
        else {
            mRequiredType = thenClause.requiredType();
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
    public Type requiredType() {
        return mRequiredType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        final Expression newThenClause = mThenClause.requiresType(type);
        final Expression newElseClause = mElseClause.requiresType(type);
        return (newThenClause == mThenClause && newElseClause == mElseClause)? this :
                new IfExpression(mCondition, newThenClause, newElseClause);
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mCondition.resolveReferences(knownTargets);
        mThenClause.resolveReferences(knownTargets);
        mElseClause.resolveReferences(knownTargets);
    }

    private Type resultingTypeRecursive(Type thenType, Type elseType) {
        if (thenType == elseType) {
            return thenType;
        }
        else if (thenType instanceof IntegerType leftType) {
            return leftType.getUnion((IntegerType) elseType);
        }
        else if (thenType instanceof ArrayType leftType) {
            final ArrayType rightType = (ArrayType) elseType;
            return new ArrayType(leftType.getLengthType().getUnion(rightType.getLengthType()), resultingTypeRecursive(leftType.getItemType(), rightType.getItemType()));
        }
        else if (thenType instanceof RegisterType leftType) {
            final RegisterType rightType = (RegisterType) elseType;
            final ImmutableMap<Token, Type> rightFields = rightType.getFields();
            ImmutableMap<String, Type> easyRightFields = ImmutableHashMap.empty();
            for (int i = 0; i < rightFields.size(); i++) {
                easyRightFields = easyRightFields.put(rightFields.keyAt(i).getText(), rightFields.valueAt(i));
            }

            final int fieldCount = leftType.getFields().size();
            ImmutableMap<Token, Type> newFields = ImmutableHashMap.empty();
            for (int i = 0; i < fieldCount; i++) {
                final Token token = leftType.getFields().keyAt(i);
                newFields = newFields.put(token, resultingTypeRecursive(leftType.getFields().valueAt(i), easyRightFields.get(token.getText())));
            }

            return new RegisterType(newFields);
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    @Override
    public Type resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        final Type thenType = mThenClause.resultingType(paramTypes, logger);
        final Type elseType = mElseClause.resultingType(paramTypes, logger);
        return resultingTypeRecursive(thenType, elseType);
    }
}
