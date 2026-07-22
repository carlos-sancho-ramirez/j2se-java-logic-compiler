package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayValueAtExpression implements Expression {
    private final Type mResultingType;
    private final Expression mArray;
    private final Expression mIndex;

    public ArrayValueAtExpression(Expression array, Expression index) {
        ensureNonNull(array, index);
        ensureValidArguments(array.resultingType() instanceof ArrayType);
        ensureValidArguments(index.resultingType() instanceof IntegerType indexType && !indexType.getMin().getText().equals("*") && !indexType.getMin().getText().startsWith("-"));
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
        final IntegerType indexType = (IntegerType) mIndex.resultingType();
        final IntegerType lengthType = new IntegerType(new Token(IntegerLiteralOperations.sum(indexType.getMin().getText(), "1")),
                indexType.getMax().getText().equals(TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(IntegerLiteralOperations.sum(indexType.getMax().getText(), "1")));
        final Expression newArray = mArray.resultTo(new ArrayType(lengthType, type));
        return (newArray == mArray)? this : new ArrayValueAtExpression(newArray, mIndex);
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        mArray.resolveReferences(knownTargets);
        mIndex.resolveReferences(knownTargets);
    }
}
