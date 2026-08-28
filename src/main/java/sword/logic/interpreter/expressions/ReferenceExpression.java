package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.ImpossibleSituationException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.interpreter.statements.ConstantDefinitionStatement.validConstantName;

public final class ReferenceExpression implements Expression {
    private final Token mReference;

    public ReferenceExpression(Token reference) {
        ensureValidArguments(validConstantName(reference.getText()));
        mReference = reference;
    }

    public Token getReference() {
        return mReference;
    }

    @Override
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return new ImmutableHashSet.Builder<String>()
                .add(mReference.getText())
                .build();
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException {
        return scope.resolveType(mReference, resolvedExpressions);
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        return restrictionMap.get(mReference.getText());
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws ImpossibleSituationException {
        final Type thisType = resolvedExpressions.get(this, null);
        if (thisType == null) {
            return null;
        }
        else {
            final Type newType = restrictionMap.get(mReference.getText(), thisType).getIntersection(resultType);
            if (newType == null) {
                throw new ImpossibleSituationException("Unable to match retricted type");
            }

            return restrictionMap.put(mReference.getText(), newType);
        }
    }
}
