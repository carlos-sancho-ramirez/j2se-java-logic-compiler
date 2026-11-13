package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayValueAtExpression implements JavaExpression {
    private final JavaExpression mArray;
    private final JavaExpression mIndex;

    public JavaArrayValueAtExpression(JavaExpression array, JavaExpression index) {
        ensureNonNull(array, index);
        mArray = array;
        mIndex = index;
    }

    @Override
    public String getText() {
        return mArray.getText() + "[" + mIndex.getText() + "]";
    }
}
