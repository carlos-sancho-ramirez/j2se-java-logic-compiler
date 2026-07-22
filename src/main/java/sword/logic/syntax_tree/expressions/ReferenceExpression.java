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
