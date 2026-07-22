package sword.logic.syntax_tree.expressions;

import org.junit.jupiter.api.Test;
import sword.collections.ImmutableHashMap;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class AdditionExpressionTest {
    private void checkResultingTypeOfTwoLiterals(String a, String b, String expectedResult) {
        final IntegerLiteralExpression leftExpression = new IntegerLiteralExpression(new Token(a));
        final IntegerLiteralExpression rightExpression = new IntegerLiteralExpression(new Token(b));
        final AdditionExpression expression = new AdditionExpression(leftExpression, rightExpression);

        final IntegerType type = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals(expectedResult, type.getMin().getText());
        assertEquals(expectedResult, type.getMax().getText());
    }

    private void checkResultingTypeOfLiteralAndReference(String a, String bMin, String bMax, String expectedMin, String expectedMax) {
        final IntegerLiteralExpression leftExpression = new IntegerLiteralExpression(new Token(a));
        final ReferenceExpression rightExpression = new ReferenceExpression(TypeConstants.unboundIntegerType, new Token("b"));
        try {
            rightExpression.resolveReferences(new ImmutableHashMap.Builder<String, ReferenceTarget>()
                    .put("b", new FunctionParameter(new Token("b"), new IntegerType(new Token(bMin), new Token(bMax))))
                    .build());
        }
        catch (UnresolvedReferenceException e) {
            fail();
        }

        final AdditionExpression expression = new AdditionExpression(leftExpression, rightExpression);

        final IntegerType type = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals(expectedMin, type.getMin().getText());
        assertEquals(expectedMax, type.getMax().getText());
    }

    @Test
    void resultingType() {
        checkResultingTypeOfTwoLiterals("7", "4", "11");
        checkResultingTypeOfTwoLiterals("-3", "4", "1");
        checkResultingTypeOfTwoLiterals("-3", "-8", "-11");
        checkResultingTypeOfTwoLiterals("3", "-8", "-5");
        checkResultingTypeOfLiteralAndReference("0", "0", "0", "0", "0");
        checkResultingTypeOfLiteralAndReference("0", "-3", "5", "-3", "5");
        checkResultingTypeOfLiteralAndReference("1", "-3", "5", "-2", "6");
        checkResultingTypeOfLiteralAndReference("-7", "-3", "5", "-10", "-2");
    }
}
