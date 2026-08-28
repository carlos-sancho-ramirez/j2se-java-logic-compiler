package sword.logic.syntax_tree.types;

import sword.collections.ImmutableHashSet;
import sword.logic.syntax_tree.Token;

public final class TypeConstants {
    public static final String ARRAY_TYPE_TEXT = "Array";
    public static final String BOOLEAN_TYPE_TEXT = "Boolean";
    public static final String INTEGER_TYPE_TEXT = "Int";
    public static final String STRING_TYPE_TEXT = "String";

    public static final String BOOLEAN_VALUE_FALSE = "FALSE";
    public static final String BOOLEAN_VALUE_TRUE = "TRUE";

    public static final EnumType booleanType = new EnumType(new Token(BOOLEAN_TYPE_TEXT), new ImmutableHashSet.Builder<Token>()
            .add(new Token(BOOLEAN_VALUE_TRUE))
            .add(new Token(BOOLEAN_VALUE_FALSE))
            .build());

    public static final String unboundText = "*";
    public static final Token unboundToken = new Token(unboundText);
    public static final IntegerType unboundIntegerType = new IntegerType(unboundToken, unboundToken);

    public static final String zeroText = "0";
    public static final Token zeroToken = new Token(zeroText);
    public static final IntegerType unboundLengthType = new IntegerType(zeroToken, unboundToken);
}
