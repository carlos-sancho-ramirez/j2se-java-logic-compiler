package sword.logic.interpreter;

public final class UnresolvedEnumValueException extends Exception {
    private final int mLine;
    private final int mColumn;

    public UnresolvedEnumValueException(Token token) {
        super("Unable to resolve enum value '" + token + "'");
        mLine = token.getLine();
        mColumn = token.getColumn();
    }

    public int getLine() {
        return mLine;
    }

    public int getColumn() {
        return mColumn;
    }
}
