package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.interpreter.statements.Statement;
import sword.logic.syntax_tree.Token;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterConstructionExpression implements Expression {
    private final Token mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructionExpression(Token type, ImmutableList<Statement> statements) {
        ensureNonNull(type);
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(s -> s.getName().getText())));
        mType = type;
        mStatements = statements;
    }

    public Token getType() {
        return mType;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }
}
