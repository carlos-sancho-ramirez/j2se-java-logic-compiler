package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class RegisterConstructor implements Expression {
    private final Token mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructor(Token type, ImmutableList<Statement> statements) {
        ensureNonNull(type, statements);
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
