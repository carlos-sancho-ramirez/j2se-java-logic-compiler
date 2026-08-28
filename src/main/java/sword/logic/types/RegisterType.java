package sword.logic.types;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableMap;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterType implements Type {
    private final Definition mDefinition;
    private final ImmutableMap<String, Type> mRestrictedFields;

    public RegisterType(Definition definition, ImmutableMap<String, Type> restrictedFields) {
        ensureNonNull(definition);
        ensureValidArguments(restrictedFields.keySet().size() == restrictedFields.size() &&
                restrictedFields.keySet().allMatch(key -> {
                    final Type defType = definition.mFields.get(key);
                    final Type restrictedType = restrictedFields.get(key);
                    return !defType.equals(restrictedType) && defType.canFit(restrictedType);
                }));
        mDefinition = definition;
        mRestrictedFields = restrictedFields;
    }

    public Definition getDefinition() {
        return mDefinition;
    }

    public ImmutableMap<String, Type> getFields() {
        return mDefinition.mFields.putAll(mRestrictedFields);
    }

    private interface Operation {
        Type apply(Type a, Type b);
    }

    private Type performOperation(Type other, Operation operation) {
        if (other instanceof RegisterType that && mDefinition == that.mDefinition) {
            final ImmutableMap<String, Type> thisFields = getFields();
            final ImmutableMap<String, Type> thatFields = that.getFields();
            final ImmutableHashMap.Builder<String, Type> builder = new ImmutableHashMap.Builder<>();
            for (String fieldName : thisFields.keySet()) {
                final Type thisFieldType = thisFields.get(fieldName);
                final Type thatFieldType = thatFields.get(fieldName);

                final Type newType = operation.apply(thisFieldType, thatFieldType);
                if (newType == null) {
                    return null;
                }
                else if (!newType.equals(mDefinition.mFields.get(fieldName))) {
                    builder.put(fieldName, newType);
                }
            }

            final ImmutableMap<String, Type> newRestrictedFields = builder.build();
            return newRestrictedFields.equalMap(mRestrictedFields)? this :
                    newRestrictedFields.equalMap(that.mRestrictedFields)? other :
                            new RegisterType(mDefinition, newRestrictedFields);
        }
        else {
            return null;
        }
    }

    @Override
    public Type getUnion(Type other) {
        return performOperation(other, Type::getUnion);
    }

    @Override
    public Type getIntersection(Type other) {
        return performOperation(other, Type::getIntersection);
    }

    @Override
    public int hashCode() {
        return mRestrictedFields.size();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof RegisterType that &&
                mDefinition == that.mDefinition &&
                mRestrictedFields.equalMap(that.mRestrictedFields);
    }

    /**
     * Allow to distinguish among registers with the same field names.
     * <p>
     * 2 Registers will be considered to be the same type if both register type points to the same definition instance.
     */
    public static final class Definition {
        private final ImmutableMap<String, Type> mFields;

        public Definition(ImmutableMap<String, Type> fields) {
            ensureValidArguments(!fields.isEmpty() && fields.keySet().size() == fields.size());
            mFields = fields;
        }

        public ImmutableMap<String, Type> getFields() {
            return mFields;
        }
    }
}
