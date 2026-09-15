package sword.logic.compiler.generator.c.types;

public final class CVoidType implements CTypeDeclaration {
    private static final CVoidType instance = new CVoidType();

    private CVoidType() {
    }

    @Override
    public String getText() {
        return "void";
    }

    public static CVoidType getInstance() {
        return instance;
    }
}
