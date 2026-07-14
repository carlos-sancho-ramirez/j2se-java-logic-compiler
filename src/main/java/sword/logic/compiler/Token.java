package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class Token {
    private final int mLine;
    private final int mColumn;
    private final String mText;

    /**
     * Creates an explicit token. So a token that is known and written within the code.
     * @param line Line where the token start is located.
     * @param column Column where the token start is located.
     * @param text Text for the token
     */
    public Token(int line, int column, String text) {
        ensureNonNull(text);
        ensureValidArguments(!text.isEmpty());
        mLine = line;
        mColumn = column;
        mText = text;
    }

    /**
     * Creates an implicit token. So a token that is known, but it is not written within the code.
     * @param text Text for the token
     */
    public Token(String text) {
        ensureNonNull(text);
        mLine = 1;
        mColumn = 1;
        mText = text;
    }

    public int getLine() {
        return mLine;
    }

    public int getColumn() {
        return mColumn;
    }

    public String getText() {
        return mText;
    }
}
