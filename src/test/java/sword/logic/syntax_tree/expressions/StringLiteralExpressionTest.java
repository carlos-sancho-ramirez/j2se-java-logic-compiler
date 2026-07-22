package sword.logic.syntax_tree.expressions;

import org.junit.jupiter.api.Test;
import sword.collections.ImmutableHashMap;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class StringLiteralExpressionTest {
    @Test
    void resultingType() {
        final StringLiteralExpression expression = new StringLiteralExpression(new Token("\"hello\""));
        final ArrayType resultingType = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals("" + ("hello".length()), resultingType.getLengthType().getMin().getText());
        assertEquals("" + ("hello".length()), resultingType.getLengthType().getMax().getText());

        final IntegerType itemType = (IntegerType) resultingType.getItemType();
        assertEquals("" + ((int) 'e'), itemType.getMin().getText());
        assertEquals("" + ((int) 'o'), itemType.getMax().getText());
    }

    @Test
    void resultingTypeWhenEmpty() {
        final StringLiteralExpression expression = new StringLiteralExpression(new Token("\"\""));
        final ArrayType resultingType = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals("0", resultingType.getLengthType().getMin().getText());
        assertEquals("0", resultingType.getLengthType().getMax().getText());
    }
}
