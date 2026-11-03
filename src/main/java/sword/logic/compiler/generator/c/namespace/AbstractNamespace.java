package sword.logic.compiler.generator.c.namespace;

abstract class AbstractNamespace implements Namespace {
    @Override
    public ScopedTypeName newType(String typeName) {
        return new ScopedTypeName(this, typeName);
    }

    @Override
    public ScopedConstantName newConstant(String constantName) {
        return new ScopedConstantName(this, constantName);
    }
}
