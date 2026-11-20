package sword.logic.compiler.generator.java.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaFieldAccessExpression implements JavaAssignableExpression {
    private final JavaExpression mClass;
    private final String mFieldName;

    public JavaFieldAccessExpression(JavaExpression javaClass, String fieldName) {
        ensureNonNull(javaClass, fieldName);
        mClass = javaClass;
        mFieldName = fieldName;
    }

    @Override
    public String getText() {
        return mClass.getText() + "." + mFieldName;
    }
}
