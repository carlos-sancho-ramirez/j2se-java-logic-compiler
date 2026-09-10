package sword.logic.compiler.generator.c.types;

public final class CCharType implements CTypeDeclaration {
    private static final CCharType instance = new CCharType();

    private CCharType() {
    }

    @Override
    public String getText() {
        return "char";
    }

    public static CCharType getInstance() {
        return instance;
    }
}
