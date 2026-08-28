package sword.logic.types;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;

import java.util.Objects;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterType implements Type {
    private final ImmutableSet<FunctionParameter> mFields;

    public RegisterType(ImmutableSet<FunctionParameter> fields) {
        ensureValidArguments(!fields.isEmpty() &&
                fields.map(FunctionParameter::getName).toSet().size() == fields.size());
        mFields = fields;
    }

    public ImmutableSet<FunctionParameter> getFields() {
        return mFields;
    }

    @Override
    public Type getUnion(Type other) {
        // For now we perform the union of each field and return null if fields do not match between registers.
        // We can consider later what exactly means a union of registers
        if (other instanceof RegisterType that && mFields.map(FunctionParameter::getName).toSet().equalSet(that.mFields.map(FunctionParameter::getName).toSet())) {
            final ImmutableHashSet.Builder<FunctionParameter> builder = new ImmutableHashSet.Builder<>();
            for (FunctionParameter param : mFields) {
                final int otherIndex = that.getFields().indexWhere(p -> p.getName().equals(param.getName()));
                final Type fieldType = param.getType();
                final FunctionParameter otherParam = that.getFields().valueAt(otherIndex);
                final Type otherFieldType = otherParam.getType();

                if (fieldType instanceof IntType intFieldType) {
                    if (otherFieldType instanceof IntType otherIntFieldType) {
                        final Type newType = intFieldType.getUnion(otherIntFieldType);
                        builder.add((newType == intFieldType) ? param :
                                (newType == otherIntFieldType) ? otherParam :
                                new FunctionParameter(param.getName(), newType));
                    }
                    else {
                        return null;
                    }
                }
                else if (fieldType instanceof ArrayType arrayFieldType) {
                    if (otherFieldType instanceof ArrayType otherArrayFieldType) {
                        final Type newType = arrayFieldType.getUnion(otherArrayFieldType);
                        builder.add((newType == arrayFieldType) ? param :
                                (newType == otherArrayFieldType) ? otherParam :
                                new FunctionParameter(param.getName(), newType));
                    }
                    else {
                        return null;
                    }
                }
                else if (fieldType instanceof RegisterType regFieldType) {
                    if (otherFieldType instanceof RegisterType otherRegFieldType) {
                        final Type newType = regFieldType.getUnion(otherRegFieldType);
                        builder.add((newType == regFieldType) ? param :
                                (newType == otherRegFieldType) ? otherParam :
                                new FunctionParameter(param.getName(), newType));
                    }
                    else {
                        return null;
                    }
                }
                else if (fieldType instanceof EnumType enumFieldType) {
                    if (otherFieldType instanceof EnumType otherEnumFieldType) {
                        final ImmutableSet<String> allValues = enumFieldType.getValues().addAll(otherEnumFieldType.getValues());
                        builder.add((allValues == enumFieldType.getValues()) ? param :
                                (allValues == otherEnumFieldType.getValues()) ? otherParam :
                                new FunctionParameter(param.getName(), new EnumType(allValues)));
                    }
                    else {
                        return null;
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }

            return new RegisterType(builder.build());
        }
        else {
            return null;
        }
    }

    @Override
    public Type getIntersection(Type other) {
        // I wonder if we should allow intersection between different registers while at least there is a field name matching between them... polymorphism?
        if (other instanceof RegisterType that && mFields.map(FunctionParameter::getName).toSet().equalSet(that.mFields.map(FunctionParameter::getName).toSet())) {
            final ImmutableList<FunctionParameter> newFields = mFields.map(field -> {
                final String fieldName = field.getName();
                final int index = that.getFields().indexWhere(f -> f.getName().equals(fieldName));
                final FunctionParameter thatField = that.getFields().valueAt(index);
                final Type newType = field.getType().getIntersection(thatField.getType());
                return (newType == field.getType())? field :
                        (newType == thatField.getType())? thatField :
                        (newType == null)? null :
                        new FunctionParameter(fieldName, newType);
            });

            return newFields.anyMatch(Objects::isNull)? null : new RegisterType(newFields.toSet());
        }
        else {
            return null;
        }
    }

    @Override
    public boolean canFit(Type type) {
        if (type instanceof RegisterType newType && mFields.map(FunctionParameter::getName).toSet().equalSet(newType.mFields.map(FunctionParameter::getName).toSet())) {
            return mFields.allMatch(field -> {
                final String fieldName = field.getName();
                final int index = newType.getFields().indexWhere(f -> f.getName().equals(fieldName));
                return field.getType().canFit(newType.getFields().valueAt(index).getType());
            });
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mFields.size();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof RegisterType that &&
                mFields.equalSet(that.mFields);
    }
}
