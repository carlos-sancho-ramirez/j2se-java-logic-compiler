package sword.logic.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableSet;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterConstructionExpression implements Expression {
    private final String mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructionExpression(String type, ImmutableList<Statement> statements) {
        ensureNonNull(type);
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(Statement::getName)));
        mType = type;
        mStatements = statements;
    }

    public String getType() {
        return mType;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = ImmutableHashSet.empty();
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.addAll(constDef.getExpression().dependencies());
            }
        }

        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.remove(constDef.getName());
            }
        }

        return result;
    }
}
