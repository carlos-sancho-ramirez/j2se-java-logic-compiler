package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayLengthExpression implements JavaExpression {
    private final JavaExpression mArray;

    public JavaArrayLengthExpression(JavaExpression array) {
        ensureNonNull(array);
        mArray = array;
    }

    @Override
    public String getText() {
        return mArray.getText() + ".length";
    }
}
