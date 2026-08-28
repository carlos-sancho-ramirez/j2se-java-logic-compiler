package sword.logic.interpreter.type.definitions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterTypeDefinition implements TypeDefinition {
    private final ImmutableList<FunctionParameter> mFields;

    public RegisterTypeDefinition(ImmutableList<FunctionParameter> fields) {
        ensureValidArguments(!fields.isEmpty() &&
                ImmutableListExtensions.noneRepeated(fields.map(f -> f.getName().getText())));
        mFields = fields;
    }

    @Override
    public RegisterType resolve(TypeAliasResolver resolver) throws UnresolvedTypeReferenceException {
        final ImmutableMap.Builder<String, Type> fieldsBuilder = new ImmutableHashMap.Builder<>();
        for (FunctionParameter param : mFields) {
            fieldsBuilder.put(param.getName().getText(), param.getType().resolve(resolver));
        }

        final RegisterType.Definition def = new RegisterType.Definition(fieldsBuilder.build());
        return new RegisterType(def, ImmutableHashMap.empty());
    }
}
