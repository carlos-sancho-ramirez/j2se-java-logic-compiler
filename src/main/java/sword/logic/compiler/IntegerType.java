package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class IntegerType implements Type {
    private final Token mMin;
    private final Token mMax;

    public IntegerType(Token min, Token max) {
        ensureNonNull(min, max);
        mMin = min;
        mMax = max;
    }
}
