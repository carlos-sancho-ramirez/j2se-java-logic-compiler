package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.statements.ConstantDefinitionStatement;
import sword.logic.interpreter.statements.Statement;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ComplexExpression implements Expression {
    private final ImmutableList<Statement> mStatements;
    private final Expression mExpression;

    public ComplexExpression(ImmutableList<Statement> statements, Expression expression) {
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(s -> s.getName().getText())));
        ensureNonNull(expression);
        mStatements = statements;
        mExpression = expression;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    public Expression getExpression() {
        return mExpression;
    }

    @Override
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        final Scope newScope = scope.createSubscopeWithStatements(mStatements);
        for (Statement statement : mStatements) {
            statement.findAllExpressions(outMap, newScope);
        }

        mExpression.findAllExpressions(outMap, newScope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mExpression.dependencies();
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.remove(constDef.getName().getText());
            }
        }

        return result;
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        return resolvedExpressions.get(mExpression, null);
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
