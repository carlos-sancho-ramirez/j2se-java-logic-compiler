package sword.logic.compiler.generator.java;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaVariable {
    public static boolean isValidVariableName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        else {
            final int length = name.length();
            char ch = name.charAt(0);
            if (ch < 'a' || ch > 'z') {
                return false;
            }
            else {
                for (int i = 1; i < length; i++) {
                    ch = name.charAt(i);
                    if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9')) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private final String mName;
    private final JavaType mType;

    public JavaVariable(String name, JavaType type) {
        ensureValidArguments(isValidVariableName(name));
        ensureNonNull(type);
        mName = name;
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public JavaType getType() {
        return mType;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaVariable that &&
                mName.equals(that.mName) &&
                mType.equals(that.mType);
    }
}
