package sword.logic.types;

import sword.collections.ImmutableSet;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class EnumType implements Type {

    public static boolean validEnumValueName(String name) {
        if (name == null) {
            return false;
        }

        final int length = name.length();
        if (length < 2) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            final char ch = name.charAt(i);
            if (ch < 'A' || ch > 'Z') {
                return false;
            }
        }

        return true;
    }

    private final ImmutableSet<String> mValues;

    public EnumType(ImmutableSet<String> values) {
        ensureValidArguments(!values.isEmpty() &&
                values.allMatch(EnumType::validEnumValueName));
        mValues = values;
    }

    public ImmutableSet<String> getValues() {
        return mValues;
    }

    public boolean isBooleanType() {
        return mValues.size() == 1 && (mValues.contains(TypeConstants.BOOLEAN_VALUE_TRUE) || mValues.contains(TypeConstants.BOOLEAN_VALUE_FALSE)) ||
                mValues.size() == 2 && mValues.contains(TypeConstants.BOOLEAN_VALUE_TRUE) && mValues.contains(TypeConstants.BOOLEAN_VALUE_FALSE);
    }

    @Override
    public Type getIntersection(Type other) {
        if (other instanceof EnumType that) {
            final ImmutableSet<String> newValues = mValues.filter(that.getValues()::contains);
            return newValues.isEmpty()? null :
                    newValues.equals(mValues)? this :
                    newValues.equals(that.getValues())? that :
                    new EnumType(newValues);
        }
        else {
            return null;
        }
    }

    @Override
    public Type getUnion(Type other) {
        // TODO: Improve this logic once the definition is hold in this type and we can distinguish between enum types
        if (other instanceof EnumType that) {
            final ImmutableSet<String> newValues = mValues.addAll(that.mValues);
            return (newValues == mValues)? this :
                    (newValues == that.mValues)? that :
                    new EnumType(newValues);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean canFit(Type type) {
        return type instanceof EnumType newType &&
                newType.getValues().allMatch(mValues::contains);
    }

    @Override
    public int hashCode() {
        return mValues.size();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EnumType that &&
                mValues.equalSet(that.mValues);
    }
}
