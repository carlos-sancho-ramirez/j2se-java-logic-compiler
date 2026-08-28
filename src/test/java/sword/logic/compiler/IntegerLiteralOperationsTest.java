package sword.logic.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IntegerLiteralOperationsTest {
    @Test
    void sum() {
        assertEquals("11", IntegerLiteralOperations.sum("7", "4"));
        assertEquals("1", IntegerLiteralOperations.sum("-3", "4"));
    }

    @Test
    void module() {
        assertEquals("0", IntegerLiteralOperations.module("160", "16"));
    }
}