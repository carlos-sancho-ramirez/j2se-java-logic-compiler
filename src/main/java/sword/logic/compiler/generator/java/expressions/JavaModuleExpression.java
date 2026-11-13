package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.generator.java.expressions.JavaMultiplicationExpression.composeText;

public final class JavaModuleExpression implements JavaExpression {
    private final JavaExpression mLeft;
    private final JavaExpression mRight;

    public JavaModuleExpression(JavaExpression left, JavaExpression right) {
        ensureNonNull(left, right);
        mLeft = left;
        mRight = right;
    }

    @Override
    public String getText() {
        return composeText(mLeft, mRight, "%");
    }
}
