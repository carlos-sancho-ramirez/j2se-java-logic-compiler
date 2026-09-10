package sword.logic.compiler.generator.c.types;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.CVariable;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CStructType implements CType {
    private final String mName;
    private final ImmutableList<CVariable> mFields;

    public CStructType(String name, ImmutableList<CVariable> fields) {
        ensureNonNull(name, fields);
        ensureValidArguments(!fields.isEmpty());
        ensureValidArguments(fields.map(CVariable::getName).toSet().size() == fields.size());
        mName = name;
        mFields = fields;
    }

    public String getName() {
        return mName;
    }

    public ImmutableList<CVariable> getFields() {
        return mFields;
    }

    @Override
    public String getText() {
        return "struct " + mName;
    }
}
