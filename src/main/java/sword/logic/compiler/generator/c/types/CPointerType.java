package sword.logic.compiler.generator.c.types;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CPointerType implements CTypeDeclaration {
    private static CPointerType charPtrInstance;
    private static CPointerType voidPtrPtrInstance;
    private final CTypeDeclaration mTargetType;

    public CPointerType(CTypeDeclaration targetType) {
        ensureNonNull(targetType);
        mTargetType = targetType;
    }

    @Override
    public String getText() {
        return mTargetType.getText() + " *";
    }

    public static CPointerType getCharPtrInstance() {
        if (charPtrInstance == null) {
            charPtrInstance = new CPointerType(CCharType.getInstance());
        }

        return charPtrInstance;
    }

    public static CPointerType getVoidPtrPtrInstance() {
        if (voidPtrPtrInstance == null) {
            voidPtrPtrInstance = new CPointerType(new CPointerType(CVoidType.getInstance()));
        }

        return voidPtrPtrInstance;
    }
}
