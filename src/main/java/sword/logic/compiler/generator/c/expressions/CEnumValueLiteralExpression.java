package sword.logic.compiler.generator.c.expressions;

import sword.logic.compiler.generator.c.namespace.NamespaceItem;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;
import static sword.logic.types.EnumType.validEnumValueName;

public final class CEnumValueLiteralExpression implements CAssignableExpression {
    private final NamespaceItem mScopedTypeName;
    private final String mValue;

    public CEnumValueLiteralExpression(NamespaceItem scopedTypeName, String value) {
        ensureValidArguments(validTypeName(scopedTypeName.last()));
        ensureValidArguments(validEnumValueName(value));
        mScopedTypeName = scopedTypeName;
        mValue = value;
    }

    public NamespaceItem getScopedTypeName() {
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
