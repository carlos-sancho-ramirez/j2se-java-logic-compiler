package sword.logic.syntax_tree.expressions;

import org.junit.jupiter.api.Test;
import sword.collections.ImmutableHashMap;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IntegerLiteralExpressionTest {

    private void checkResultingType(String literal) {
        final IntegerLiteralExpression expression = new IntegerLiteralExpression(new Token(literal));
        final IntegerType resultingType = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals(literal, resultingType.getMin().getText());
    }

    @Test
    void resultingType() {
        checkResultingType("0");
        checkResultingType("1");
        checkResultingType("2");
        checkResultingType("24");
        checkResultingType("-3");
        checkResultingType("0x18");
    }
}