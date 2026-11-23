package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;
import sword.logic.compiler.generator.java.types.JavaType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaArrayConstructionExpression implements JavaExpression {
    private final JavaType mItemType;
    private final ImmutableList<JavaExpression> mValues;

    public JavaArrayConstructionExpression(JavaType itemType, ImmutableList<JavaExpression> values) {
        ensureNonNull(itemType, values);
        mItemType = itemType;
        mValues = values;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "new " + mItemType.getTypeText(importedClassReferences) + "[] {" + mValues.map(v -> v.getText(importedClassReferences)).reduce((a, b) -> a + ", " + b, "") + "}";
    }
}
