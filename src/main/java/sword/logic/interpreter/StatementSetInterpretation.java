package sword.logic.interpreter;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.interpreter.statements.Statement;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StatementSetInterpretation implements Interpretation {
    private final ImmutableList<Statement> mStatements;

    public StatementSetInterpretation(ImmutableList<Statement> statements) {
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(s -> s.getName().getText())));
        mStatements = statements;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }
}
