package sword.logic.compiler;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class Token {
    private final int mLine;
    private final int mColumn;
    private final String mText;

    public Token(int line, int column, String text) {
        ensureNonNull(text);
        ensureValidArguments(!text.isEmpty());
        mLine = line;
        mColumn = column;
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
