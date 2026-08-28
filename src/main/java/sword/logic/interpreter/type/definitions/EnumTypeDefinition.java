package sword.logic.interpreter.type.definitions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.interpreter.Token;
import sword.logic.types.EnumType;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.types.EnumType.validEnumValueName;

public final class EnumTypeDefinition implements TypeDefinition {
    private final ImmutableList<Token> mValues;

    public EnumTypeDefinition(ImmutableList<Token> values) {
        ensureValidArguments(values.size() >= 2 &&
                values.map(Token::getText).toSet().size() == values.size() &&
                values.allMatch(v -> validEnumValueName(v.getText())));
        mValues = values;
    }

    @Override
    public EnumType resolve(TypeAliasResolver resolver) {
        final ImmutableSet<String> values = mValues.map(Token::getText).toSet();
        return new EnumType(new EnumType.Definition(values), values);
    }
}
