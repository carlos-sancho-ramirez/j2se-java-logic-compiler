package sword.logic.interpreter.type.definitions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.types.RegisterType;

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
        return new RegisterType(ImmutableListExtensions.mapThrowing(mFields,
                 param -> new sword.logic.types.FunctionParameter(param.getName().getText(), param.getType().resolve(resolver)))
                .toSet());
    }
}
