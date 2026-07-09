package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayValueAtExpression implements Expression {
    private final Expression mArray;
    private final Expression mIndex;

    public ArrayValueAtExpression(Expression array, Expression index) {
        ensureNonNull(array, index);
        mArray = array;
        mIndex = index;
    }

    public Expression getArray() {
        return mArray;
    }

    public Expression getIndex() {
        return mIndex;
    }
}
