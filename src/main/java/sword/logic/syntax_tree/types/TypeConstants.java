package sword.logic.syntax_tree.types;

import sword.collections.ImmutableHashSet;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.EnumType;
import sword.logic.syntax_tree.types.IntegerType;

public final class TypeConstants {
    public static final EnumType booleanType = new EnumType(new Token("Boolean"), new ImmutableHashSet.Builder<Token>()
            .add(new Token("TRUE"))
            .add(new Token("FALSE"))
            .build());

    public static final String unboundText = "*";
    public static final Token unboundToken = new Token(unboundText);
    public static final IntegerType unboundIntegerType = new IntegerType(unboundToken, unboundToken);

    public static final String zeroText = "0";
    public static final Token zeroToken = new Token(zeroText);
    public static final IntegerType unboundLengthType = new IntegerType(zeroToken, unboundToken);
}
