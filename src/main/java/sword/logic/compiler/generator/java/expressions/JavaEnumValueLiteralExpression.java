package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaEnumValue;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaEnumValueLiteralExpression implements JavaExpression {
    private final JavaEnumValue mEnumValue;

    public JavaEnumValueLiteralExpression(JavaEnumValue enumValue) {
        ensureNonNull(enumValue);
        mEnumValue = enumValue;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return mEnumValue.getEnum().getName() + "." + mEnumValue.getName();
    }
}
