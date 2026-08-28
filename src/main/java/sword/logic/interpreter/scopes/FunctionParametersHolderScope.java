package sword.logic.interpreter.scopes;

import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.type.definitions.FunctionParameter;
import sword.logic.interpreter.Token;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

/**
 * Scope for a function body, where new parameters and its types has been added to the existing ones.
 */
public final class FunctionParametersHolderScope extends AbstractScope {
    private final AbstractScope mParent;
    private final ImmutableSet<FunctionParameter> mParameters;

    FunctionParametersHolderScope(AbstractScope parent, ImmutableSet<FunctionParameter> parameters) {
        ensureNonNull(parent);
        ensureValidArguments(!parameters.isEmpty() && ImmutableListExtensions.noneRepeated(parameters.map(FunctionParameter::getName)));
        mParent = parent;
        mParameters = parameters;
    }

    @Override
    public boolean knowsConstantName(String constantName) {
        return mParameters.anyMatch(param -> param.getName().getText().equals(constantName)) ||
                mParent.knowsConstantName(constantName);
    }

    @Override
    public EnumType resolveEnumValue(Token value) throws UnresolvedEnumValueException {
        return mParent.resolveEnumValue(value);
    }

    @Override
    public Type resolveTypeAlias(Token typeAlias) throws UnresolvedTypeReferenceException {
        return mParent.resolveTypeAlias(typeAlias);
    }

    @Override
    Type resolveDefinedType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        for (FunctionParameter parameter : mParameters) {
            if (parameter.getName().getText().equals(constantName.getText())) {
                return parameter.getType().resolve(this);
            }
        }

        return mParent.resolveDefinedType(constantName, resolvedExpressions);
    }

    @Override
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        Type definedHereType = null;
        for (FunctionParameter parameter : mParameters) {
            if (parameter.getName().getText().equals(constantName.getText())) {
                definedHereType = parameter.getType().resolve(this);
            }
        }

        if (definedHereType == null) {
            return mParent.resolveType(constantName, resolvedExpressions, restrictionMap);
        }
        else if (restrictionMap.containsKey(constantName.getText())) {
            final Type restrictedType = restrictionMap.get(constantName.getText());
            ensureValidState(definedHereType.canFit(restrictedType));
            return restrictedType;
        }
        else {
            return definedHereType;
        }
    }

    @Override
    public Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        return resolveDefinedType(constantName, resolvedExpressions);
    }
}
