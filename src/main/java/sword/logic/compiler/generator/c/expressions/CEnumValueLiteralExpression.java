package sword.logic.compiler.generator.c.expressions;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;
import static sword.logic.types.EnumType.validEnumValueName;

public final class CEnumValueLiteralExpression implements CAssignableExpression {
    private final ImmutableList<String> mScopedTypeName;
    private final String mValue;

    public CEnumValueLiteralExpression(ImmutableList<String> scopedTypeName, String value) {
        ensureValidArguments(validTypeName(scopedTypeName.last()));
        ensureValidArguments(validEnumValueName(value));
        mScopedTypeName = scopedTypeName;
        mValue = value;
    }

    public ImmutableList<String> getScopedTypeName() {
        return mScopedTypeName;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public String getText() {
        return mScopedTypeName.reduce((a, b) -> a + "_" + b) + "_" + mValue;
    }
}
