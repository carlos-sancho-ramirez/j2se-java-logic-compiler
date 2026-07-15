package sword.logic.syntax_tree.expressions;

import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayValueAtExpression implements Expression {
    private final Type mResultingType;
    private final Expression mArray;
    private final Expression mIndex;

    public ArrayValueAtExpression(Expression array, Expression index) {
        ensureNonNull(array, index);
        ensureValidArguments(array.resultingType() instanceof ArrayType);
        ensureValidArguments(index.resultingType() instanceof IntegerType);
        mArray = array;
        mIndex = index;
        mResultingType = ((ArrayType) array.resultingType()).getItemType();
    }

    public Expression getArray() {
        return mArray;
    }

    public Expression getIndex() {
        return mIndex;
    }

    @Override
    public Type resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        final Expression newArray = mArray.resultTo(new ArrayType(type));
        return (newArray == mArray)? this : new ArrayValueAtExpression(newArray, mIndex);
    }
}
