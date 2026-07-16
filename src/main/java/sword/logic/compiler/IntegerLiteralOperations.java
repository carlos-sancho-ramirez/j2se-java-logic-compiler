package sword.logic.compiler;

public final class IntegerLiteralOperations {
    private static String toDecimal(String value) {
        final char first = value.charAt(0);
        if (first >= '1' && first <= '9' || first == '0' && value.length() == 1) {
            return value;
        }
        else if (first == '-' && value.length() >= 2 && value.charAt(1) >= '1' && value.charAt(1) <= '9') {
            return "-" + toDecimal(value.substring(1));
        }
        else if (value.length() > 2 && first == '0' && value.charAt(1) == 'x') {
            String result = "0";
            for (int i = 2; i < value.length(); i++) {
                char ch = value.charAt(i);
                int v;
                if (ch >= '0' && ch <= '9') {
                    v = ch - '0';
                }
                else if (ch >= 'A' && ch <= 'F') {
                    v = ch - 'A' + 10;
                }
                else if (ch >= 'a' && ch <= 'f') {
                    v = ch - 'a' + 10;
                }
                else {
                    throw new UnsupportedOperationException("Invalid hex literal value '" + value + "'");
                }

                result = sum(multiplicationForPositiveDecimalNumbers(result, "16"), "" + v);
            }

            return result;
        }
        else {
            throw new UnsupportedOperationException("Missing implementation to transform integer literals starting " + first);
        }
    }

    private static boolean greaterThanForPositiveDecimalNumbers(String a, String b) {
        if (a.length() > b.length()) {
            return true;
        }
        else if (b.length() > a.length()) {
            return false;
        }
        else {
            for (int i = 0; i < a.length(); i++) {
                final char chA = a.charAt(i);
                final char chB = b.charAt(i);
                if (chA > chB) {
                    return true;
                }
                else if (chA < chB) {
                    return false;
                }
            }

            return false;
        }
    }

    public static boolean greaterThan(String a, String b) {
        a = toDecimal(a);
        b = toDecimal(b);

        if (a.charAt(0) != '-') {
            return b.charAt(0) == '-' || greaterThanForPositiveDecimalNumbers(a, b);
        }
        else {
            return b.charAt(0) == '-' && greaterThanForPositiveDecimalNumbers(b.substring(1), a.substring(1));
        }
    }

    public static boolean greaterOrEqualThan(String a, String b) {
        return !greaterThan(b, a);
    }

    public static boolean lowerThan(String a, String b) {
        return greaterThan(b, a);
    }

    public static boolean lowerOrEqualThan(String a, String b) {
        return !greaterThan(a, b);
    }

    public static String min(String a, String b) {
        return greaterThan(a, b)? b : a;
    }

    public static String max(String a, String b) {
        return greaterThan(a, b)? a : b;
    }

    private static String sumForPositiveDecimalNumbers(String a, String b) {
        while (a.length() > b.length()) {
            b = "0" + b;
        }

        while (b.length() > a.length()) {
            a = "0" + a;
        }

        final int length = a.length();
        String result = "";
        boolean carry = false;
        for (int i = length - 1; i >= 0; i--) {
            int v = (a.charAt(i) - '0') + (b.charAt(i) - '0');
            if (carry) {
                v++;
            }

            if (v < 10) {
                result = "" + ((char) ('0' + v)) + result;
                carry = false;
            }
            else {
                result = "" + ((char) ('0' + v - 10)) + result;
                carry = true;
            }
        }

        if (carry) {
            result = "1" + result;
        }

        return result;
    }

    private static String subtractionForPositiveDecimalNumbers(String a, String b) {
        while (a.length() > b.length()) {
            b = "0" + b;
        }

        while (b.length() > a.length()) {
            a = "0" + a;
        }

        final int length = a.length();
        String result = "";
        boolean carry = false;
        for (int i = length - 1; i >= 0; i--) {
            int chA = a.charAt(i) - '0';
            int chB = b.charAt(i) - '0';
            if (carry) {
                chB++;
            }

            if (chA >= chB) {
                result = "" + ((char)(chA - chB + '0')) + result;
                carry = false;
            }
            else {
                result = "" + ((char)(chA + 10 - chB + '0')) + result;
                carry = true;
            }
        }

        if (carry) {
            return "-" + subtractionForPositiveDecimalNumbers(b, a);
        }
        else {
            while (result.length() > 1 && result.charAt(0) == '0') {
                result = result.substring(1);
            }

            return result;
        }
    }

    public static String sum(String a, String b) {
        a = toDecimal(a);
        b = toDecimal(b);

        if (a.charAt(0) != '-') {
            if (b.charAt(0) != '-') {
                return sumForPositiveDecimalNumbers(a, b);
            }
            else {
                return subtractionForPositiveDecimalNumbers(a, b.substring(1));
            }
        }
        else {
            if (b.charAt(0) != '-') {
                return subtractionForPositiveDecimalNumbers(b, a.substring(1));
            }
            else {
                return "-" + sumForPositiveDecimalNumbers(a.substring(1), b.substring(1));
            }
        }
    }

    public static String subtraction(String a, String b) {
        a = toDecimal(a);
        b = toDecimal(b);

        if (a.charAt(0) != '-') {
            if (b.charAt(0) == '-') {
                return sumForPositiveDecimalNumbers(a, b.substring(1));
            }
            else {
                return subtractionForPositiveDecimalNumbers(a, b);
            }
        }
        else {
            if (b.charAt(0) == '-') {
                return subtractionForPositiveDecimalNumbers(b.substring(1), a.substring(1));
            }
            else {
                return "-" + sumForPositiveDecimalNumbers(a.substring(1), b);
            }
        }
    }

    private static String multiplicationForPositiveDecimalNumbers(String a, String b) {
        String total = "0";
        for (int i = b.length() - 1; i >= 0; i--) {
            String lineResult = "0".repeat(b.length() - 1 - i);

            int retained = 0;
            for (int j = a.length() -1; j >= 0; j--) {
                final int cypherMul = (a.charAt(j) - '0') * (b.charAt(i) - '0') + retained;
                lineResult = "" + (char)((cypherMul % 10) + '0') + lineResult;
                retained = cypherMul / 10;
            }

            if (retained > 0) {
                lineResult = "" + (char)(retained + '0') + lineResult;
            }

            while (lineResult.length() > 1 && lineResult.charAt(0) == '0') {
                lineResult = lineResult.substring(1);
            }

            total = sumForPositiveDecimalNumbers(total, lineResult);
        }

        return total;
    }

    public static String multiplication(String a, String b) {
         a = toDecimal(a);
         b = toDecimal(b);

         if (a.charAt(0) != '-') {
             return (b.charAt(0) != '-')?
                    multiplicationForPositiveDecimalNumbers(a, b) :
                    "-" + multiplicationForPositiveDecimalNumbers(a, b.substring(1));
         }
         else {
             final String aPositive = a.substring(1);
             return (b.charAt(0) != '-')?
                    "-" + multiplicationForPositiveDecimalNumbers(aPositive, b) :
                    multiplicationForPositiveDecimalNumbers(aPositive, b.substring(1));
         }
    }

    private static String divisionForPositiveDecimalNumbers(String a, String b) {
        String result = "";
        String dividend = "";
        boolean somethingSet = false;
        for (int i = 0; i < a.length(); i++) {
            dividend = dividend + a.charAt(i);
            if (!greaterThanForPositiveDecimalNumbers(b, dividend)) {
                int count = 0;
                final String lastDividend = dividend;
                while ((dividend = subtractionForPositiveDecimalNumbers(dividend, b)).charAt(0) != '-') {
                    count++;
                }

                final String toSubtract = multiplicationForPositiveDecimalNumbers("" + (char)(count + '0'), b);
                dividend = subtractionForPositiveDecimalNumbers(lastDividend, toSubtract);
                result = result + (char)(count + '0');
                somethingSet = true;
            }
            else if (somethingSet) {
                result += "0";
            }
        }

        return somethingSet? result : "0";
    }

    public static String division(String a, String b) {
        a = toDecimal(a);
        b = toDecimal(b);

        if (a.charAt(0) != '-') {
            if (b.charAt(0) != '-') {
                return divisionForPositiveDecimalNumbers(a, b);
            }
            else {
                final String result = divisionForPositiveDecimalNumbers(a, b.substring(1));
                return result.equals("0")? result : "-" + result;
            }
        }
        else {
            final String aPositive = a.substring(1);
            if (b.charAt(0) != '-') {
                final String result = divisionForPositiveDecimalNumbers(aPositive, b);
                return result.equals("0")? result : "-" + result;
            }
            else {
                return divisionForPositiveDecimalNumbers(aPositive, b.substring(1));
            }
        }
    }

    private IntegerLiteralOperations() {
    }
}
