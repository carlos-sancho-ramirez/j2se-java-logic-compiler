package sword.logic.compiler.generator.c.namespace;

abstract class AbstractNamespace implements Namespace {
    @Override
    public NameSubspace newSubspace(String name) {
        return new NameSubspace(this, name);
    }

    @Override
    public ScopedTypeName newType(String typeName) {
        return new ScopedTypeName(this, typeName);
    }

    @Override
    public ScopedConstantName newConstant(String constantName) {
        return new ScopedConstantName(this, constantName);
    }
}
