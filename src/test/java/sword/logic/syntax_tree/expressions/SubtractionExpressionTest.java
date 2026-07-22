package sword.logic.syntax_tree.expressions;

import org.junit.jupiter.api.Test;
import sword.collections.ImmutableHashMap;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.TypeConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class SubtractionExpressionTest {
    private void checkResultingTypeOfTwoLiterals(String a, String b, String expectedResult) {
        final IntegerLiteralExpression leftExpression = new IntegerLiteralExpression(new Token(a));
        final IntegerLiteralExpression rightExpression = new IntegerLiteralExpression(new Token(b));
        final SubtractionExpression expression = new SubtractionExpression(leftExpression, rightExpression);

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

        final SubtractionExpression expression = new SubtractionExpression(leftExpression, rightExpression);

        final IntegerType type = expression.resultingType(ImmutableHashMap.empty(), msg -> {});
        assertEquals(expectedMin, type.getMin().getText());
        assertEquals(expectedMax, type.getMax().getText());
    }

    @Test
    void resultingType() {
        checkResultingTypeOfTwoLiterals("7", "4", "3");
        checkResultingTypeOfTwoLiterals("-3", "4", "-7");
        checkResultingTypeOfTwoLiterals("-3", "-8", "5");
        checkResultingTypeOfTwoLiterals("3", "-8", "11");
        checkResultingTypeOfLiteralAndReference("0", "0", "0", "0", "0");
        checkResultingTypeOfLiteralAndReference("0", "-3", "5", "-5", "3");
        checkResultingTypeOfLiteralAndReference("1", "-3", "5", "-4", "4");
        checkResultingTypeOfLiteralAndReference("-7", "-3", "5", "-12", "-4");
    }
}
