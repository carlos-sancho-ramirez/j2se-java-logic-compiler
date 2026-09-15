package sword.logic.compiler.generator.c.types;

public final class CUnsignedCharType implements CTypeDeclaration {
    private static final CUnsignedCharType instance = new CUnsignedCharType();

    private CUnsignedCharType() {
    }

    @Override
    public String getText() {
        return "unsigned char";
    }

    public static CUnsignedCharType getInstance() {
        return instance;
    }
}
