package sword.logic.compiler;

import sword.logic.syntax_tree.Token;

public final class UnresolvedReferenceException extends Exception {
    private final int mLine;
    private final int mColumn;

    public UnresolvedReferenceException(Token token) {
        super("Unresolved reference " + token.getText());
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
