package sword.logic.compiler.generator.c.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CPointerType implements CTypeDeclaration {
    private final CTypeDeclaration mTargetType;

    public CPointerType(CTypeDeclaration targetType) {
        ensureNonNull(targetType);
        mTargetType = targetType;
    }

    @Override
    public String getText() {
        return mTargetType.getText() + " *";
    }
}
