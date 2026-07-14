package sword.logic.compiler;

import sword.collections.ImmutableHashSet;

final class ExpressionConstants {
    static final EnumType booleanType = new EnumType(new Token("Boolean"), new ImmutableHashSet.Builder<Token>()
            .add(new Token("TRUE"))
            .add(new Token("FALSE"))
            .build());

    static final String unboundText = "*";
    static final Token unboundToken = new Token(unboundText);
    static final IntegerType unboundIntegerType = new IntegerType(unboundToken, unboundToken);
}
