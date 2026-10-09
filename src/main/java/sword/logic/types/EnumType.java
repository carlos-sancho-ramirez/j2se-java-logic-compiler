package sword.logic.types;

import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
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

        char ch = name.charAt(0);
        if (ch < 'A' || ch > 'Z') {
            return false;
        }

        for (int i = 1; i < length - 1; i++) {
            ch = name.charAt(i);
            if ((ch < 'A' || ch > 'Z') && ch != '_') {
                return false;
            }
        }

        ch = name.charAt(length - 1);
        return ch >= 'A' && ch <= 'Z';
    }

    private final Definition mDefinition;
    private final ImmutableSet<String> mValues;

    public EnumType(Definition definition, ImmutableSet<String> values) {
        ensureNonNull(definition);
        ensureValidArguments(!values.isEmpty() &&
                values.allMatch(definition.getPossibleValues()::contains));
        mDefinition = definition;
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
        if (other instanceof EnumType that && mDefinition == that.mDefinition) {
            final ImmutableSet<String> newValues = mValues.filter(that.getValues()::contains);
            return newValues.isEmpty()? null :
                    newValues.equals(mValues)? this :
                    newValues.equals(that.getValues())? that :
                    new EnumType(mDefinition, newValues);
        }
        else {
            return null;
        }
    }

    @Override
    public Type getUnion(Type other) {
        if (other instanceof EnumType that && mDefinition == that.mDefinition) {
            final ImmutableSet<String> newValues = mValues.addAll(that.mValues);
            return (newValues == mValues)? this :
                    (newValues == that.mValues)? that :
                    new EnumType(mDefinition, newValues);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean canFit(Type type) {
        return type instanceof EnumType newType &&
                mDefinition == newType.mDefinition &&
                newType.getValues().allMatch(mValues::contains);
    }

    @Override
    public int hashCode() {
        return mValues.size();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EnumType that &&
                mDefinition == that.mDefinition &&
                mValues.equalSet(that.mValues);
    }

    public Definition getDefinition() {
        return mDefinition;
    }

    /**
     * Allow to distinguish among enums.
     */
    public static final class Definition {
        private final ImmutableSet<String> mPossibleValues;

        public Definition(ImmutableSet<String> possibleValues) {
            ensureValidArguments(possibleValues.size() >= 2 && possibleValues.allMatch(EnumType::validEnumValueName));
            mPossibleValues = possibleValues;
        }

        public ImmutableSet<String> getPossibleValues() {
            return mPossibleValues;
        }
    }
}
