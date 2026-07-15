package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.logic.syntax_tree.statements.Statement;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.RegisterType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterConstructor implements Expression {
    private final Type mResultingType;
    private final Token mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructor(Type resultingType, Token type, ImmutableList<Statement> statements) {
        ensureNonNull(resultingType, type, statements);
        ensureValidArguments(resultingType instanceof RegisterType || resultingType == UnknownType.getInstance());
        mResultingType = resultingType;
        mType = type;
        mStatements = statements;
    }

    public Token getType() {
        return mType;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    @Override
    public Type resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type instanceof RegisterType regType && mResultingType instanceof RegisterType resType && regType.getFields().equals(resType.getFields())) {
            return this;
        }
        else if (mResultingType == UnknownType.getInstance() && type instanceof RegisterType regType) {
            return new RegisterConstructor(regType, mType, mStatements);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ".");
        }
    }
}
