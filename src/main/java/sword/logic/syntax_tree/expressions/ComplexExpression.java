package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.logic.syntax_tree.statements.Statement;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ComplexExpression implements Expression {
    private final ImmutableList<Statement> mStatements;
    private final Expression mExpression;

    public ComplexExpression(ImmutableList<Statement> statements, Expression expression) {
        ensureValidArguments(!statements.isEmpty());
        mStatements = statements;
        mExpression = expression;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    @Override
    public Type resultingType() {
        return mExpression.resultingType();
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        final Expression newExpression = mExpression.resultTo(type);
        return (newExpression == mExpression)? this : new ComplexExpression(mStatements, newExpression);
    }
}
