package sword.logic.interpreter;

public final class UnresolvedTypeReferenceException extends Exception {
    private final int mLine;
    private final int mColumn;

    public UnresolvedTypeReferenceException(Token token) {
        super("Unable to resolve type '" + token + "'");
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
