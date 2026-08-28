package sword.logic.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableSet;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ComplexExpression implements Expression {
    private final ImmutableList<Statement> mStatements;
    private final Expression mExpression;

    public ComplexExpression(ImmutableList<Statement> statements, Expression expression) {
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(Statement::getName)));
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
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = mExpression.dependencies();
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.remove(constDef.getName());
            }
        }

        return result;
    }
}
