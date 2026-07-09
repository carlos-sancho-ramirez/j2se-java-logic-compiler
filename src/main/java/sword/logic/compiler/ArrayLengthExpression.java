package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayLengthExpression implements Expression {
    private final Expression mArray;

    public ArrayLengthExpression(Expression array) {
        ensureNonNull(array);
        mArray = array;
    }

    public Expression getArray() {
        return mArray;
    }
}
