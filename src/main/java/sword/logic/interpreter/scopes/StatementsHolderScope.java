package sword.logic.interpreter.scopes;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableMap;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.statements.ConstantDefinitionStatement;
import sword.logic.interpreter.statements.Statement;
import sword.logic.interpreter.statements.TypeDefinitionStatement;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

/**
 * Holds new constants defined.
 */
public final class StatementsHolderScope extends AbstractScope {
    private final AbstractScope mParent;
    private final ImmutableSet<Statement> mStatements;
    private final MutableMap<TypeDefinitionStatement, Type> mResolvedTypeAlias = MutableHashMap.empty();

    StatementsHolderScope(AbstractScope parent, ImmutableSet<Statement> statements) {
        ensureNonNull(parent);
        ensureValidArguments(!statements.isEmpty() && ImmutableListExtensions.noneRepeated(statements.map(Statement::getName)));
        mParent = parent;
        mStatements = statements;
    }

    @Override
    public boolean knowsConstantName(String constantName) {
        return mStatements.anyMatch(st -> st instanceof ConstantDefinitionStatement constDef && constDef.getName().getText().equals(constantName)) ||
                mParent.knowsConstantName(constantName);
    }

    @Override
    public Type resolveTypeAlias(Token typeAlias) throws UnresolvedTypeReferenceException {
        for (Statement statement : mStatements) {
            if (statement instanceof TypeDefinitionStatement typeDef) {
                if (typeDef.getName().getText().equals(typeAlias.getText())) {
                    final Type cached = mResolvedTypeAlias.get(typeDef, null);
                    if (cached == null) {
                        final Type resolved = typeDef.getDefinition().resolve(this);
                        mResolvedTypeAlias.put(typeDef, resolved);
                        return resolved;
                    }
                    else {
                        return cached;
                    }
                }
            }
        }

        return mParent.resolveTypeAlias(typeAlias);
    }

    @Override
    Type resolveDefinedType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                if (constDef.getName().getText().equals(constantName.getText())) {
                    return resolvedExpressions.get(constDef.getExpression(), null);
                }
            }
        }

        return mParent.resolveDefinedType(constantName, resolvedExpressions);
    }

    @Override
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        Type definedHereType = null;
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                if (constDef.getName().getText().equals(constantName.getText())) {
                    definedHereType = resolvedExpressions.get(constDef.getExpression(), null);
                    if (definedHereType == null) {
                        return null;
                    }
                }
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
        return resolveType(constantName, resolvedExpressions, ImmutableHashMap.empty());
    }
}
