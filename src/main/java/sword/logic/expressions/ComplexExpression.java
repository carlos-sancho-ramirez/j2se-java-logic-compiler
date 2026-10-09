package sword.logic.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableSet;
import sword.collections.MutableHashMap;
import sword.collections.MutableMap;
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
        final MutableMap<String, ImmutableSet<String>> checkedStatements = MutableHashMap.empty();

        int iterationIndex = 0;
        boolean somethingChanged;
        do {
            if (iterationIndex++ >= 50) {
                throw new RuntimeException("Infinite loop?");
            }
            somethingChanged = false;

            outerLoop:
            for (String dependency : result) {
                final ImmutableSet<String> cached = checkedStatements.get(dependency, null);
                if (cached != null) {
                    result = result.remove(dependency).addAll(cached);
                    somethingChanged = true;
                    break;
                }
                else {
                    for (Statement statement : mStatements) {
                        if (dependency.equals(statement.getName()) && statement instanceof ConstantDefinitionStatement constDef) {
                            final ImmutableSet<String> statementDependencies = constDef.getExpression().dependencies();
                            checkedStatements.put(statement.getName(), statementDependencies);
                            result = result.remove(dependency).addAll(statementDependencies);
                            somethingChanged = true;
                            break outerLoop;
                        }
                    }
                }
            }
        }
        while (somethingChanged);

        return result;
    }
}
