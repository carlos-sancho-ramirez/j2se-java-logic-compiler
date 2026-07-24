package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.collections.Procedure;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.statements.ConstantDefinitionStatement;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.FunctionType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.IntegerLiteralOperations.greaterOrEqualThan;
import static sword.logic.compiler.IntegerLiteralOperations.lowerOrEqualThan;
import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ReferenceExpression implements Expression {
    private final Type mResultingType;
    private final Token mReference;
    private ReferenceTarget mTarget;

    public ReferenceExpression(Type resultingType, Token reference) {
        ensureNonNull(resultingType, reference);
        mResultingType = resultingType;
        mReference = reference;
    }

    public ReferenceTarget getTarget() {
        return mTarget;
    }

    @Override
    public Type requiredType() {
        return mResultingType;
    }

    private static Type resultingTypeRecursive(Type type, Type current) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return current;
        }
        else if (type instanceof IntegerType intType) {
            if (current == UnknownType.getInstance()) {
                return intType;
            }
            else if (current instanceof IntegerType currentIntType) {
                final String intTypeMin = intType.getMin().getText();
                final String intTypeMax = intType.getMax().getText();
                final String currentMin = currentIntType.getMin().getText();
                final String currentMax = currentIntType.getMax().getText();
                if ((currentMax.equals("*") || !intTypeMax.equals("*") && lowerOrEqualThan(intTypeMax, currentMax)) &&
                        (currentMin.equals("*") || !intTypeMin.equals("*") && greaterOrEqualThan(intTypeMin, currentMin))) {
                    return intType;
                }
                else {
                    throw new TypeMismatchException("Integer type from " + intTypeMin + " to " + intTypeMax + " does not fit in " + currentMin + ".." + currentMax);
                }
            }
            else {
                throw new TypeMismatchException("Type " + current.getClass().getSimpleName() + " cannot be converted to " + type.getClass().getSimpleName());
            }
        }
        else if (type instanceof ArrayType arrayType) {
            if (current == UnknownType.getInstance()) {
                return arrayType;
            }
            else if (current instanceof ArrayType currentArrayType) {
                final String lengthTypeMin = arrayType.getLengthType().getMin().getText();
                final String lengthTypeMax = arrayType.getLengthType().getMax().getText();
                final String currentLengthMin = currentArrayType.getLengthType().getMin().getText();
                final String currentLengthMax = currentArrayType.getLengthType().getMax().getText();
                if ((currentLengthMax.equals("*") || !lengthTypeMax.equals("*") && lowerOrEqualThan(lengthTypeMax, currentLengthMax)) &&
                        (currentLengthMin.equals("*") || !lengthTypeMin.equals("*") && greaterOrEqualThan(lengthTypeMin, currentLengthMin))) {
                    final Type newItemType = resultingTypeRecursive(arrayType.getItemType(), currentArrayType.getItemType());
                    return (newItemType == currentArrayType.getItemType())? current :
                            new ArrayType(arrayType.getLengthType(), newItemType);
                }
                else {
                    throw new TypeMismatchException("Integer type from " + lengthTypeMin + " to " + lengthTypeMax + " does not fit in " + currentLengthMin + ".." + currentLengthMax);
                }
            }
            else {
                throw new TypeMismatchException("Type " + current.getClass().getSimpleName() + " cannot be converted to " + type.getClass().getSimpleName());
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented for type " + type.getClass().getSimpleName());
        }
    }

    public Token getReference() {
        return mReference;
    }

    private Expression resultToType(Type current, Type type, Type newType) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (current == UnknownType.getInstance()) {
            return new ReferenceExpression(newType, mReference);
        }
        else if (type instanceof IntegerType && current instanceof IntegerType) {
            return this;
        }
        else if (type instanceof ArrayType arrayType && current instanceof ArrayType currentArrayType) {
            return resultToType(currentArrayType.getItemType(), arrayType.getItemType(), newType);
        }
        else if (type instanceof FunctionType funcType && current instanceof FunctionType currentFuncType) {
            // TODO: We should take the parameters into account as well
            return resultToType(currentFuncType.getResultType(), funcType.getResultType(), newType);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already resolved to " + mResultingType.getClass().getSimpleName());
        }
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        return resultToType(mResultingType, type, type);
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        if (mTarget == null) {
            final ReferenceTarget target = knownTargets.get(mReference.getText(), null);
            if (target == null) {
                throw new UnresolvedReferenceException(mReference);
            }
            else {
                mTarget = target;
            }
        }
    }

    @Override
    public Type resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        if (mTarget instanceof FunctionParameter funcParam) {
            return paramTypes.get(funcParam.getName().getText(), funcParam.getType());
        }
        else {
            final ConstantDefinitionStatement constDef = (ConstantDefinitionStatement) mTarget;
            return constDef.getExpression().resultingType(paramTypes, logger);
        }
    }
}
