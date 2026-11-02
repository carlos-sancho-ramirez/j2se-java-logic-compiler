package sword.logic.compiler.generator.c;

import sword.collections.ImmutableIntValueMap;
import sword.logic.expressions.StringLiteralExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

final class StringPool {
    private final String mData;
    private final ImmutableIntValueMap<StringLiteralExpression> mIndexes;

    StringPool(String data, ImmutableIntValueMap<StringLiteralExpression> indexes) {
        ensureNonNull(data, indexes);
        mData = data;
        mIndexes = indexes;
    }

    String getData() {
        return mData;
    }

    ImmutableIntValueMap<StringLiteralExpression> getIndexes() {
        return mIndexes;
    }
}
