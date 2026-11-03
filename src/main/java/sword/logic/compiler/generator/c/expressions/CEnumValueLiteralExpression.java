package sword.logic.compiler.generator.c.expressions;

import sword.logic.compiler.generator.c.namespace.ScopedTypeName;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.types.EnumType.validEnumValueName;

public final class CEnumValueLiteralExpression implements CAssignableExpression {
    private final ScopedTypeName mScopedTypeName;
    private final String mValue;

    public CEnumValueLiteralExpression(ScopedTypeName scopedTypeName, String value) {
        ensureNonNull(scopedTypeName);
        ensureValidArguments(validEnumValueName(value));
        mScopedTypeName = scopedTypeName;
        mValue = value;
    }

    public ScopedTypeName getScopedTypeName() {
        return mScopedTypeName;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public String getText() {
        return mScopedTypeName.symbolText() + "_" + mValue;
    }
}
