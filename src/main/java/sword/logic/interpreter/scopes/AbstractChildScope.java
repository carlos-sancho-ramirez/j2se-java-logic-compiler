package sword.logic.interpreter.scopes;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

abstract class AbstractChildScope extends AbstractScope {
    final AbstractScope mParent;

    AbstractChildScope(AbstractScope parent) {
        ensureNonNull(parent);
        mParent = parent;
    }

    @Override
    public boolean isFileRootScope() {
        return mParent == BuiltInScope.getInstance();
    }

    @Override
    public boolean contains(Scope other) {
        return other instanceof AbstractChildScope childScope &&
                (this == childScope.mParent || contains(childScope.mParent));
    }
}
