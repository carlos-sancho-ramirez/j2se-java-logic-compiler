package sword.logic.compiler.generator.java.entities;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.types.EnumType.validEnumValueName;

public final class JavaEnumValue {
    private final JavaEnum mEnum;
    private final String mName;

    JavaEnumValue(JavaEnum javaEnum, String name) {
        ensureNonNull(javaEnum);
        ensureValidArguments(validEnumValueName(name));
        mEnum = javaEnum;
        mName = name;
    }

    public JavaEnum getEnum() {
        return mEnum;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaEnumValue that &&
                mEnum.equals(that.mEnum) &&
                mName.equals(that.mName);
    }
}
