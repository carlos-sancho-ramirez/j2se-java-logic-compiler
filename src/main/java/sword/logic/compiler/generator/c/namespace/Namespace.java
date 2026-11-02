package sword.logic.compiler.generator.c.namespace;

public interface Namespace {
    static Namespace getFileRoot() {
        return FileRootNamespace.getInstance();
    }

    NamespaceItem append(String item);
}
