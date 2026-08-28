package sword.logic.interpreter.types;

import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterTypeDefinition implements TypeDefinition {
    private final ImmutableList<FunctionParameter> mFields;

    public RegisterTypeDefinition(ImmutableList<FunctionParameter> fields) {
        ensureValidArguments(!fields.isEmpty() &&
                ImmutableListExtensions.noneRepeated(fields.map(f -> f.getName().getText())));
        mFields = fields;
    }
}
