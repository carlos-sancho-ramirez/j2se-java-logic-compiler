package com.example;

public final class Main {
    private static int passedCount = 0;
    private static int failureCount = 0;

    private static void testOk(int ch0, int ch1, String expected) {
        final Result result = Output.disasmInstruction(new ImmutableIntList.Builder()
                .append(ch0)
                .append(ch1)
                .build());

        if (result.isValid()) {
            if (expected.equals(result.getMessage())) {
                System.out.println("  * Ok: " + result.getMessage());
                passedCount++;
            }
            else {
                System.out.println("  * Failure: Expected '" + expected + "' but it was '" + result.getMessage() + "'");
                failureCount++;
            }
        }
        else {
            System.out.println("  * Failure: Expected '" + expected + "' but returned error with text '" + result.getMessage() + "'");
            failureCount++;
        }
    }

    public static void main(String[] args) {
        testOk(0x00, 0x00, "add [bx + si], al");

        final int allTestCount = passedCount + failureCount;
        System.out.println(allTestCount + " tests found. " + passedCount + " passed, " + failureCount + " failed");
    }
}