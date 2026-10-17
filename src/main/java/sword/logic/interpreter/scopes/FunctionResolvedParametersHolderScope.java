package sword.logic.interpreter.scopes;

import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Token;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.types.FunctionParameter;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

/**
 * Scope for a function body, where new parameters and its types has been added to the existing ones.
 */
public final class FunctionResolvedParametersHolderScope extends AbstractChildScope {
    private final ImmutableMap<Token, Type> mParameters;

    FunctionResolvedParametersHolderScope(AbstractScope parent, ImmutableMap<Token, Type> parameters) {
        super(parent);
        ensureValidArguments(!parameters.isEmpty());
        mParameters = parameters;
    }

    @Override
    public boolean knowsConstantName(String constantName) {
        return mParameters.keySet().anyMatch(token -> token.getText().equals(constantName)) ||
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
        for (Token token : mParameters.keySet()) {
            if (token.getText().equals(constantName.getText())) {
                return mParameters.get(token);
            }
        }

        return mParent.resolveDefinedType(constantName, resolvedExpressions);
    }

    @Override
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        Type definedHereType = null;
        for (Token token : mParameters.keySet()) {
            if (token.getText().equals(constantName.getText())) {
                definedHereType = mParameters.get(token);
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
