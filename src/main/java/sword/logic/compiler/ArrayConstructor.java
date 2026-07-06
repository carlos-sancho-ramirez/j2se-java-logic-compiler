package sword.logic.compiler;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayConstructor implements Expression {
    private final Token mType;
    private final ImmutableList<Expression> mValues;

    public ArrayConstructor(Token type, ImmutableList<Expression> values) {
        ensureNonNull(type, values);
        mType = type;
        mValues = values;
    }
}
