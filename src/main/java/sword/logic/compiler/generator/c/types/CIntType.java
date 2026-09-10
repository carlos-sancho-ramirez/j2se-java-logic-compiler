package sword.logic.compiler.generator.c.types;

public final class CIntType implements CTypeDeclaration {
    private static final CIntType instance = new CIntType();

    private CIntType() {
    }

    @Override
    public String getText() {
        return "int";
    }

    public static CIntType getInstance() {
        return instance;
    }
}
