package sword.logic.compiler.generator.c.namespace;

public interface Namespace {
    static Namespace getFileRoot() {
        return FileRootNamespace.getInstance();
    }

    NameSubspace newSubspace(String name);
    ScopedTypeName newType(String typeName);
    ScopedConstantName newConstant(String constantName);
}
