package sword.logic.compiler.generator.c.namespace;

final class FileRootNamespace implements Namespace {
    private static final FileRootNamespace INSTANCE = new FileRootNamespace();

    static FileRootNamespace getInstance() {
        return INSTANCE;
    }

    private FileRootNamespace() {
    }

    @Override
    public NamespaceItem append(String item) {
        return new NamespaceItem(this, item);
    }

    @Override
    public String toString() {
        return "FileRootNamespace";
    }
}
