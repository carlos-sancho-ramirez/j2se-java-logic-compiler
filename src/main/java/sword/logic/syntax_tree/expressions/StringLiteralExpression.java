package sword.logic.syntax_tree.expressions;

import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StringLiteralExpression implements Expression {
    private final Token mLiteral;

    public StringLiteralExpression(Token literal) {
        ensureValidArguments(literal.getText().charAt(0) == '"');
        mLiteral = literal;
    }

    public Token getLiteral() {
        return mLiteral;
    }

    @Override
    public Type resultingType() {
        return new ArrayType(new IntegerType(new Token("0"), new Token("127")));
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof ArrayType arrayType) {
            if (arrayType.getItemType() == UnknownType.getInstance() || arrayType.getItemType() instanceof IntegerType) {
                return this;
            }
            else {
                throw new TypeMismatchException("Unable to resolve this expression to " + type + ". It is already an Array[Int]");
            }
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type + ". It is already an Array[Int]");
        }
    }
}
