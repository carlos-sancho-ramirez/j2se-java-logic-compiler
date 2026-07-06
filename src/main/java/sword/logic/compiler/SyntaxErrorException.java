package sword.logic.compiler;

public final class SyntaxErrorException extends Exception {
    private final int mLine;
    private final int mColumn;

    public SyntaxErrorException(String message, int line, int column) {
        super(message);
        mLine = line;
        mColumn = column;
    }

    public int getLine() {
        return mLine;
    }

    public int getColumn() {
        return mColumn;
    }
}
