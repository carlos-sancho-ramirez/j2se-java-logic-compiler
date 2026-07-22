package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IntegerLiteralExpression implements Expression {
    private IntegerType mResultingType;
    private final Token mLiteral;

    public IntegerLiteralExpression(Token literal) {
        ensureValidArguments(literal.getText().charAt(0) >= '0' && literal.getText().charAt(0) <= '9');
        mLiteral = literal;
    }

    public Token getLiteral() {
        return mLiteral;
    }

    @Override
    public IntegerType resultingType() {
        if (mResultingType == null) {
            mResultingType = new IntegerType(new Token(mLiteral.getText()), new Token(mLiteral.getText()));
        }

        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type instanceof IntegerType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already an Integer");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        // Nothing to resolve
    }
}
