package sword.logic.compiler;

public final class SemanticErrorException extends Exception {
    private final int mLine;
    private final int mColumn;

    public SemanticErrorException(String message, int line, int column) {
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
