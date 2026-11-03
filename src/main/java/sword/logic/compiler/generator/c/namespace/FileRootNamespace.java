package sword.logic.compiler.generator.c.namespace;

final class FileRootNamespace extends AbstractNamespace {
    private static final FileRootNamespace INSTANCE = new FileRootNamespace();

    static FileRootNamespace getInstance() {
        return INSTANCE;
    }

    private FileRootNamespace() {
    }

    @Override
    public NameSubspace newSubspace(String name) {
        return new NameSubspace(this, name);
    }

    @Override
    public String toString() {
        return "FileRootNamespace";
    }
}
