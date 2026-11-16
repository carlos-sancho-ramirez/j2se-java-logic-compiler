package sword.logic.compiler.generator.java.expressions;

import sword.logic.compiler.generator.java.JavaEnumValue;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaEnumValueLiteralExpression implements JavaExpression {
    private final JavaEnumValue mEnumValue;

    public JavaEnumValueLiteralExpression(JavaEnumValue enumValue) {
        ensureNonNull(enumValue);
        mEnumValue = enumValue;
    }

    @Override
    public String getText() {
        return mEnumValue.getEnum().getName() + "." + mEnumValue.getName();
    }
}
