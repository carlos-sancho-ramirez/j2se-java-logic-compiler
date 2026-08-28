package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.interpreter.statements.Statement;

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
}
