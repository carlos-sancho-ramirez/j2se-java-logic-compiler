package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.ImpossibleSituationException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.BuiltInScope;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.FunctionType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

public final class LeftRightExpression implements Expression {
    private final Token mOperator;
    private final Expression mLeft;
    private final Expression mRight;

    public LeftRightExpression(Token operator, Expression left, Expression right) {
        ensureValidArguments(
                operator.getText().equals("*") ||
                operator.getText().equals("/") ||
                operator.getText().equals("%") ||
                operator.getText().equals("+") ||
                operator.getText().equals("-") ||
                operator.getText().equals("==") ||
                operator.getText().equals("!=") ||
                operator.getText().equals(">=") ||
                operator.getText().equals("<=") ||
                operator.getText().equals(">") ||
                operator.getText().equals("<") ||
                operator.getText().equals("&") ||
                operator.getText().equals("|"));
        ensureNonNull(left, right);
        mOperator = operator;
        mLeft = left;
        mRight = right;
    }

    @Override
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mLeft.findAllExpressions(outMap, scope);
        if (mOperator.getText().equals("&")) {
            mRight.findAllExpressions(outMap, scope.whenTrue(mLeft));
        }
        else if (mOperator.getText().equals("|")) {
            mRight.findAllExpressions(outMap, scope.whenFalse(mLeft));
        }
        else {
            mRight.findAllExpressions(outMap, scope);
        }
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mLeft.dependencies().addAll(mRight.dependencies());
    }

    private IntType resolveForAddition(IntType leftType, IntType rightType) {
        IntType result = null;
        for (IntType.Range leftRange : leftType.getRanges()) {
            for (IntType.Range rightRange : rightType.getRanges()) {
                final String leftMin = leftRange.getMin();
                final String leftMax = leftRange.getMax();
                final String rightMin = rightRange.getMin();
                final String rightMax = rightRange.getMax();
                final boolean leftMinUnbound = leftMin.equals(TypeConstants.unboundText);
                final boolean leftMaxUnbound = leftMax.equals(TypeConstants.unboundText);
                final boolean rightMinUnbound = rightMin.equals(TypeConstants.unboundText);
                final boolean rightMaxUnbound = rightMax.equals(TypeConstants.unboundText);
                if ((leftMinUnbound || rightMinUnbound) && (leftMaxUnbound || rightMaxUnbound)) {
                    return IntType.getAnyIntType();
                }
                else if (leftMinUnbound || rightMinUnbound) {
                    final IntType thisType = new IntType(TypeConstants.unboundText, IntegerLiteralOperations.sum(leftMax, rightMax));
                    result = (result == null)? thisType : result.getUnion(thisType);
                }
                else {
                    final String resultingMinBound = IntegerLiteralOperations.sum(leftMin, rightMin);
                    final String resultingMaxBound = (leftMaxUnbound || rightMaxUnbound) ? TypeConstants.unboundText :
                            IntegerLiteralOperations.sum(leftMax, rightMax);

                    final IntType thisType = new IntType(resultingMinBound, resultingMaxBound);
                    result = (result == null)? thisType : result.getUnion(thisType);
                }
            }
        }

        ensureValidState(result != null);
        return result;
    }

    private IntType resolveForSubtraction(IntType leftType, IntType rightType) {
        if (leftType.getRanges().size() == 1 && rightType.getRanges().size() == 1) {
            final String leftMin = leftType.getRanges().first().getMin();
            final String leftMax = leftType.getRanges().last().getMax();
            final String rightMin = rightType.getRanges().first().getMin();
            final String rightMax = rightType.getRanges().last().getMax();
            final boolean leftMinUnbound = leftMin.equals(TypeConstants.unboundText);
            final boolean leftMaxUnbound = leftMax.equals(TypeConstants.unboundText);
            final boolean rightMinUnbound = rightMin.equals(TypeConstants.unboundText);
            final boolean rightMaxUnbound = rightMax.equals(TypeConstants.unboundText);

            // TODO: Improve this logic to delimit integer ranges
            if (leftMinUnbound || leftMaxUnbound || rightMinUnbound || rightMaxUnbound) {
                return new IntType(
                        TypeConstants.unboundText,
                        TypeConstants.unboundText);
            }
            else {
                return new IntType(
                        IntegerLiteralOperations.subtraction(leftMin, rightMax),
                        IntegerLiteralOperations.subtraction(leftMax, rightMin));
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private IntType resolveForMultiplication(IntType leftType, IntType rightType) {
        if (leftType.getRanges().size() == 1 && rightType.getRanges().size() == 1) {
            final String leftMin = leftType.getRanges().first().getMin();
            final boolean leftMinIsNegative = leftMin.charAt(0) == '-';
            final String leftMax = leftType.getRanges().last().getMax();
            final boolean leftMaxIsNegative = leftMax.charAt(0) == '-';
            final String rightMin = rightType.getRanges().first().getMin();
            final boolean rightMinIsNegative = rightMin.charAt(0) == '-';
            final String rightMax = rightType.getRanges().last().getMax();
            final boolean rightMaxIsNegative = rightMax.charAt(0) == '-';

            // TODO: We should improve this logic when unbound integers are present. We should be able to delimit this further.
            if (leftMin.equals(TypeConstants.unboundText) || leftMax.equals(TypeConstants.unboundText) ||
                    rightMin.equals(TypeConstants.unboundText) || rightMax.equals(TypeConstants.unboundText)) {
                return new IntType(TypeConstants.unboundText, TypeConstants.unboundText);
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

                return new IntType(newMin, newMax);
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private IntType resolveForDivision(IntType leftType, IntType rightType) {
        if (rightType.getRanges().size() == 1) {
            if (leftType.getRanges().size() == 1) {
                final String leftMin = leftType.getRanges().first().getMin();
                final String leftMax = leftType.getRanges().last().getMax();
                final String rightMin = rightType.getRanges().first().getMin();
                final String rightMax = rightType.getRanges().last().getMax();

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

                return new IntType(newMin, newMax);
            }
            else {
                return leftType.getRanges()
                        .map(r -> resolveForDivision(new IntType(new ImmutableList.Builder<IntType.Range>().append(r).build()), rightType))
                        .reduce(IntType::getUnion);
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented resolveForDivision for " + leftType + " and " + rightType);
        }
    }

    private ArrayType resolveForConcatenation(ArrayType leftType, ArrayType rightType) throws SemanticErrorException {
        final IntType newLengthType = resolveForAddition(leftType.getLengthType(), rightType.getLengthType());
        final Type newItemType = leftType.getItemType().getUnion(rightType.getItemType());
        return (newLengthType == leftType.getLengthType() && newItemType == leftType.getItemType())? leftType :
                (newLengthType == rightType.getLengthType() && newItemType == rightType.getItemType())? rightType :
                new ArrayType(newLengthType, newItemType);
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        final Type rawLeftType = resolvedExpressions.get(mLeft, null);
        final Type rawRightType = resolvedExpressions.get(mRight, null);
        if (rawLeftType == null || rawRightType == null) {
            return null;
        }
        else {
            final String operatorText = mOperator.getText();
            if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%") || operatorText.equals("-")) {
                if (rawLeftType instanceof IntType leftType) {
                    if (rawRightType instanceof IntType rightType) {
                        if (operatorText.equals("*")) {
                            return resolveForMultiplication(leftType, rightType);
                        }
                        else if (operatorText.equals("/")) {
                            return resolveForDivision(leftType, rightType);
                        }
                        else if (operatorText.equals("%")) {
                            ensureValidArguments(IntegerLiteralOperations.greaterThan(rightType.getRanges().first().getMin(), "0"));
                            final String newMax = IntegerLiteralOperations.subtraction(rightType.getRanges().last().getMax(), "1");
                            ensureValidArguments(newMax.charAt(0) != '-');
                            return new IntType(TypeConstants.zeroText, newMax);
                        }
                        else {
                            return resolveForSubtraction(leftType, rightType);
                        }
                    }
                    else {
                        throw new SemanticErrorException("Right side is not a number", mOperator.getLine(), mOperator.getColumn());
                    }
                }
                else {
                    throw new SemanticErrorException("Left side is not a number", mOperator.getLine(), mOperator.getColumn());
                }
            }
            else if (operatorText.equals("+")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    return resolveForAddition(leftType, rightType);
                }
                else if (rawLeftType instanceof ArrayType leftType && rawRightType instanceof ArrayType rightType) {
                    return resolveForConcatenation(leftType, rightType);
                }
                else {
                    throw new SemanticErrorException("Expected either a numeric expression or an array in both sides of the operator", mOperator.getLine(), mOperator.getColumn());
                }
            }
            else if (operatorText.equals("==")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerThan(leftMax, rightMin) || !rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && leftMin.equals(leftMax) && leftMin.equals(rightMin) && leftMin.equals(rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (operatorText.equals("!=")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerThan(leftMax, rightMin) || !rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && leftMin.equals(leftMax) && leftMin.equals(rightMin) && leftMin.equals(rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (operatorText.equals(">=")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (operatorText.equals("<=")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (operatorText.equals(">")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerOrEqualThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (operatorText.equals("<")) {
                if (rawLeftType instanceof IntType leftType && rawRightType instanceof IntType rightType) {
                    // TODO: Reshape this logic for multiple ranges in IntType
                    final String leftMin = leftType.getRanges().first().getMin();
                    final String leftMax = leftType.getRanges().last().getMax();
                    final String rightMin = rightType.getRanges().first().getMin();
                    final String rightMax = rightType.getRanges().last().getMax();

                    final BuiltInScope builtInScope = BuiltInScope.getInstance();
                    if (leftMin.equals(TypeConstants.unboundText)) {
                        if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (leftMax.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else if (rightMin.equals(TypeConstants.unboundText)) {
                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }
                    }
                    else {
                        if (IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                            throw new SemanticErrorException("This expression is always false", mOperator.getLine(), mOperator.getColumn());
                        }

                        if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                            throw new SemanticErrorException("This expression is always true", mOperator.getLine(), mOperator.getColumn());
                        }
                    }

                    return builtInScope.getBooleanType();
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else {
                ensureValidState(operatorText.equals("&") || operatorText.equals("|"));
                return BuiltInScope.getInstance().getBooleanType();
            }
        }
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        final String operatorText = mOperator.getText();
        if (operatorText.equals("*") || operatorText.equals("/") || operatorText.equals("%") || operatorText.equals("-")) {
            final IntType leftType = (IntType) mLeft.resolveType(restrictionMap);
            final IntType rightType = (IntType) mRight.resolveType(restrictionMap);
            if (operatorText.equals("*")) {
                return resolveForMultiplication(leftType, rightType);
            }
            else if (operatorText.equals("/")) {
                return resolveForDivision(leftType, rightType);
            }
            else if (operatorText.equals("%")) {
                if (rightType.hasUniqueValue() && !leftType.getRanges().first().getMin().equals(TypeConstants.unboundText) && !rightType.getRanges().last().getMax().equals(TypeConstants.unboundText)) {
                    final String rightValue = rightType.getRanges().first().getMin();
                    IntType result = null;
                    for (IntType.Range range : leftType.getRanges()) {
                        for (String value : range) {
                            final String newValue = IntegerLiteralOperations.module(value, rightValue);
                            result = (result == null)? IntType.withUniqueValue(newValue) : result.include(newValue);
                        }
                    }

                    ensureValidState(result != null);
                    return result;
                }
                else {
                    ensureValidArguments(IntegerLiteralOperations.greaterThan(rightType.getRanges().first().getMin(), "0"));
                    final String newMax = IntegerLiteralOperations.subtraction(rightType.getRanges().last().getMax(), "1");
                    ensureValidArguments(newMax.charAt(0) != '-');
                    return new IntType(TypeConstants.zeroText, newMax);
                }
            }
            else {
                return resolveForSubtraction(leftType, rightType);
            }
        }
        else if (operatorText.equals("==")) {
            // TODO: Reshape this logic for multiple ranges in IntType
            final IntType leftType = (IntType) mLeft.resolveType(restrictionMap);
            final IntType rightType = (IntType) mRight.resolveType(restrictionMap);
            final String leftMin = leftType.getRanges().first().getMin();
            final String leftMax = leftType.getRanges().last().getMax();
            final String rightMin = rightType.getRanges().first().getMin();
            final String rightMax = rightType.getRanges().last().getMax();

            final BuiltInScope builtInScope = BuiltInScope.getInstance();
            if (leftMin.equals(TypeConstants.unboundText)) {
                if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else if (leftMax.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else if (rightMin.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else {
                if (IntegerLiteralOperations.lowerThan(leftMax, rightMin) || !rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }

                if (!rightMax.equals(TypeConstants.unboundText) && leftMin.equals(leftMax) && leftMin.equals(rightMin) && leftMin.equals(rightMax)) {
                    return builtInScope.getAlwaysTrueType();
                }
            }

            return builtInScope.getBooleanType();
        }
        else if (operatorText.equals(">=")) {
            final IntType leftType = (IntType) mLeft.resolveType(restrictionMap);
            final IntType rightType = (IntType) mRight.resolveType(restrictionMap);
            // TODO: Reshape this logic for multiple ranges in IntType
            final String leftMin = leftType.getRanges().first().getMin();
            final String leftMax = leftType.getRanges().last().getMax();
            final String rightMin = rightType.getRanges().first().getMin();
            final String rightMax = rightType.getRanges().last().getMax();

            final BuiltInScope builtInScope = BuiltInScope.getInstance();
            if (leftMin.equals(TypeConstants.unboundText)) {
                if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else if (leftMax.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysTrueType();
                }
            }
            else if (rightMin.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysTrueType();
                }
            }
            else {
                if (IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysFalseType();
                }

                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysTrueType();
                }

                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }

            return builtInScope.getBooleanType();
        }
        else if (operatorText.equals("<")) {
            // TODO: Reshape this logic for multiple ranges in IntType
            final IntType leftType = (IntType) mLeft.resolveType(restrictionMap);
            final IntType rightType = (IntType) mRight.resolveType(restrictionMap);
            final String leftMin = leftType.getRanges().first().getMin();
            final String leftMax = leftType.getRanges().last().getMax();
            final String rightMin = rightType.getRanges().first().getMin();
            final String rightMax = rightType.getRanges().last().getMax();

            final BuiltInScope builtInScope = BuiltInScope.getInstance();
            if (leftMin.equals(TypeConstants.unboundText)) {
                if (!leftMax.equals(TypeConstants.unboundText) && !rightMin.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysTrueType();
                }
            }
            else if (leftMax.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else if (rightMin.equals(TypeConstants.unboundText)) {
                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }
            }
            else {
                if (IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysTrueType();
                }

                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.greaterOrEqualThan(leftMin, rightMax)) {
                    return builtInScope.getAlwaysFalseType();
                }

                if (!rightMax.equals(TypeConstants.unboundText) && IntegerLiteralOperations.lowerThan(leftMax, rightMin)) {
                    return builtInScope.getAlwaysTrueType();
                }
            }

            return builtInScope.getBooleanType();
        }
        else if (operatorText.equals("&")) {
            final EnumType leftType = (EnumType) mLeft.resolveType(restrictionMap);
            final EnumType rightType = (EnumType) mRight.resolveType(restrictionMap);
            return (leftType.getValues().size() == 1 && leftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE) &&
                    rightType.getValues().size() == 1 && rightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE))? BuiltInScope.getInstance().getAlwaysTrueType() :
                    (leftType.getValues().size() == 1 && leftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE) ||
                    rightType.getValues().size() == 1 && rightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE))? BuiltInScope.getInstance().getAlwaysFalseType() :
                    BuiltInScope.getInstance().getBooleanType();
        }
        else if (operatorText.equals("|")) {
            final EnumType leftType = (EnumType) mLeft.resolveType(restrictionMap);
            final EnumType rightType = (EnumType) mRight.resolveType(restrictionMap);
            return (leftType.getValues().size() == 1 && leftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE) ||
                    rightType.getValues().size() == 1 && rightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE))? BuiltInScope.getInstance().getAlwaysTrueType() :
                    (leftType.getValues().size() == 1 && leftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE) &&
                            rightType.getValues().size() == 1 && rightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE))? BuiltInScope.getInstance().getAlwaysFalseType() :
                            BuiltInScope.getInstance().getBooleanType();
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws ImpossibleSituationException {
        final Type thisType = resolvedExpressions.get(this, null);
        if (thisType == null) {
            return null;
        }
        else {
            final Type leftType = resolvedExpressions.get(mLeft, null);
            final Type rightType = resolvedExpressions.get(mRight, null);
            if (mOperator.getText().equals("%")) {
                if (leftType instanceof IntType leftIntType && rightType instanceof IntType rightIntType) {
                    if (mRight.isIndependent() && rightIntType.hasUniqueValue() && resultType instanceof IntType resultIntType && !leftIntType.getRanges().first().getMin().equals(TypeConstants.unboundText) && !leftIntType.getRanges().last().getMax().equals(TypeConstants.unboundText)) {
                        final String rightValue = rightIntType.getRanges().first().getMin();
                        ImmutableMap<String, Type> accRestrictionMap = null;
                        for (IntType.Range range : leftIntType.getRanges()) {
                            for (String value : range) {
                                if (resultIntType.contains(IntegerLiteralOperations.module(value, rightValue))) {
                                    try {
                                        final ImmutableMap<String, Type> newMap = mLeft.restrictionMap(IntType.withUniqueValue(value), resolvedExpressions, restrictionMap);
                                        if (accRestrictionMap == null) {
                                            accRestrictionMap = newMap;
                                        }
                                        else {
                                            final ImmutableMap<String, Type> oldMap = accRestrictionMap;
                                            accRestrictionMap = oldMap.keySet().addAll(newMap.keySet()).assign(name -> {
                                                if (!oldMap.containsKey(name)) {
                                                    return newMap.get(name);
                                                }
                                                else if (!newMap.containsKey(name)) {
                                                    return oldMap.get(name);
                                                }
                                                else {
                                                    return newMap.get(name).getUnion(oldMap.get(name));
                                                }
                                            });
                                        }
                                    }
                                    catch (ImpossibleSituationException e) {
                                        // It's OK
                                    }
                                }
                            }
                        }

                        if (accRestrictionMap == null) {
                            throw new ImpossibleSituationException("Unable to return " + resultType + " with the current restrictions");
                        }
                        else {
                            return accRestrictionMap;
                        }
                    }
                    else {
                        // We can do very few here if integers are unbound
                        // TODO: Improve this when possible/required
                        return restrictionMap;
                    }
                }
                else {
                    throw new RuntimeException("Operator '%' requires numbers on each side");
                }
            }
            else if (mOperator.getText().equals("==")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof IntType) {
                        if (rightType instanceof IntType rightIntType) {
                            if (mRight.isIndependent() && rightIntType.hasUniqueValue()) {
                                final String rightValue = rightIntType.getRanges().first().getMin();
                                if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                                    return mLeft.restrictionMap(IntType.withUniqueValue(rightValue), resolvedExpressions, restrictionMap);
                                }
                                else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                    return mLeft.restrictionMap(IntType.getAnyIntType().exclude(rightValue), resolvedExpressions, restrictionMap);
                                }
                                else {
                                    throw new UnsupportedOperationException("Unimplemented");
                                }
                            }
                            else {
                                throw new UnsupportedOperationException("Unimplemented");
                            }
                        }
                        else {
                            throw new RuntimeException("Incomparable types");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else if (mOperator.getText().equals("!=")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof IntType) {
                        if (rightType instanceof IntType rightIntType) {
                            if (mRight.isIndependent() && rightIntType.hasUniqueValue()) {
                                final String rightValue = rightIntType.getRanges().first().getMin();
                                if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                                    return mLeft.restrictionMap(IntType.getAnyIntType().exclude(rightValue), resolvedExpressions, restrictionMap);
                                }
                                else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                    return mLeft.restrictionMap(IntType.withUniqueValue(rightValue), resolvedExpressions, restrictionMap);
                                }
                                else {
                                    throw new UnsupportedOperationException("Unimplemented");
                                }
                            }
                            else {
                                throw new UnsupportedOperationException("Unimplemented");
                            }
                        }
                        else {
                            throw new RuntimeException("Incomparable types");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else if (mOperator.getText().equals(">=")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof IntType leftIntType) {
                        if (rightType instanceof IntType rightIntType) {
                            if (mRight.isIndependent() && rightIntType.hasUniqueValue()) {
                                final String rightValue = rightIntType.getRanges().first().getMin();
                                final IntType newType;
                                if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                                    newType = new IntType(rightValue, leftIntType.getRanges().last().getMax());
                                }
                                else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                    newType = new IntType(leftIntType.getRanges().first().getMin(), IntegerLiteralOperations.subtraction(rightValue, "1"));
                                }
                                else {
                                    throw new UnsupportedOperationException("Unimplemented");
                                }

                                return mLeft.restrictionMap(newType, resolvedExpressions, restrictionMap);
                            }
                            else {
                                throw new UnsupportedOperationException("Unimplemented");
                            }
                        }
                        else {
                            throw new RuntimeException("Incomparable types");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else if (mOperator.getText().equals("<")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof IntType leftIntType) {
                        if (rightType instanceof IntType rightIntType) {
                            if (mRight.isIndependent() && rightIntType.hasUniqueValue()) {
                                if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                                    final String rightValue = rightIntType.getRanges().first().getMin();
                                    final IntType newType = new IntType(leftIntType.getRanges().first().getMin(), IntegerLiteralOperations.subtraction(rightValue, "1"));
                                    return mLeft.restrictionMap(newType, resolvedExpressions, restrictionMap);
                                }
                                else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                    final String rightValue = rightIntType.getRanges().first().getMin();
                                    final IntType newType = new IntType(rightValue, leftIntType.getRanges().last().getMax());
                                    return mLeft.restrictionMap(newType, resolvedExpressions, restrictionMap);
                                }
                                else {
                                    throw new UnsupportedOperationException("Unimplemented");
                                }
                            }
                            else {
                                throw new UnsupportedOperationException("Unimplemented");
                            }
                        }
                        else {
                            throw new RuntimeException("Incomparable types");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else if (mOperator.getText().equals("&")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof EnumType) {
                        ensureValidState(rightType instanceof EnumType);
                        if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                            final ImmutableMap<String, Type> newRestrictionMap = mLeft.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                            return (newRestrictionMap == null)? null : mRight.restrictionMap(resultType, resolvedExpressions, newRestrictionMap);
                        }
                        else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                            final EnumType newLeftType;
                            if (mLeft.dependencies().filterNot(restrictionMap.keySet()::contains).isEmpty()) {
                                newLeftType = (EnumType) mLeft.resolveType(restrictionMap);
                            }
                            else {
                                newLeftType = (EnumType) leftType;
                            }

                            if (newLeftType.getValues().size() == 1 && newLeftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                                return mRight.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                            }
                            else {
                                final EnumType newRightType;
                                if (mRight.dependencies().filterNot(restrictionMap.keySet()::contains).isEmpty()) {
                                    newRightType = (EnumType) mRight.resolveType(restrictionMap);
                                }
                                else {
                                    newRightType = (EnumType) leftType;
                                }

                                return (newRightType.getValues().size() == 1 && newRightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE))?
                                        mLeft.restrictionMap(resultType, resolvedExpressions, restrictionMap) : restrictionMap;
                            }
                        }
                        else {
                            throw new UnsupportedOperationException("Unimplemented");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else if (mOperator.getText().equals("|")) {
                if (resultType instanceof EnumType resultEnumType) {
                    if (leftType instanceof EnumType) {
                        ensureValidState(rightType instanceof EnumType);
                        if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_TRUE)) {
                            final EnumType newLeftType;
                            if (mLeft.dependencies().filterNot(restrictionMap.keySet()::contains).isEmpty()) {
                                newLeftType = (EnumType) mLeft.resolveType(restrictionMap);
                            }
                            else {
                                newLeftType = (EnumType) leftType;
                            }

                            if (newLeftType.getValues().size() == 1 && newLeftType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                return mRight.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                            }
                            else {
                                final EnumType newRightType;
                                if (mRight.dependencies().filterNot(restrictionMap.keySet()::contains).isEmpty()) {
                                    newRightType = (EnumType) mRight.resolveType(restrictionMap);
                                }
                                else {
                                    newRightType = (EnumType) leftType;
                                }

                                if (newRightType.getValues().size() == 1 && newRightType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                                    return mLeft.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                                }
                                else {
                                    final ImmutableMap<String, Type> leftRestrictionMap = mLeft.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                                    final ImmutableMap<String, Type> rightRestrictionMap = mRight.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                                    if (leftRestrictionMap.keySet().equals(rightRestrictionMap.keySet()) && leftRestrictionMap.allMatch(t -> t instanceof IntType) && rightRestrictionMap.allMatch(t -> t instanceof IntType)) {
                                        return leftRestrictionMap.keySet().assign(name -> ((IntType) leftRestrictionMap.get(name)).getUnion((IntType) rightRestrictionMap.get(name)));
                                    }
                                    else {
                                        return restrictionMap;
                                    }
                                }
                            }
                        }
                        else if (resultEnumType.getValues().size() == 1 && resultEnumType.getValues().valueAt(0).equals(TypeConstants.BOOLEAN_VALUE_FALSE)) {
                            final ImmutableMap<String, Type> newRestrictionMap = mLeft.restrictionMap(resultType, resolvedExpressions, restrictionMap);
                            return (newRestrictionMap == null)? null : mRight.restrictionMap(resultType, resolvedExpressions, newRestrictionMap);
                        }
                        else {
                            throw new UnsupportedOperationException("Unimplemented");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    throw new ImpossibleSituationException("This expression always results in a Boolean expression");
                }
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
    }
}
