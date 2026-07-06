package sword.logic.compiler;

import java.io.IOException;
import java.io.InputStream;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class TokenParser {
    private interface State {
        int WAITING_FOR_TOKEN = 0;
        int PARSING_TOKEN = 1;
        int LAST_WAS_EQUALS = 2;
        int LAST_WAS_DOT = 3;
        int LAST_WAS_MINUS = 4;
        int LAST_WAS_SLASH = 5;
        int LAST_WAS_LOWER_THAN = 6;
        int LAST_WAS_GREATER_THAN = 7;
        int LAST_WAS_EXCLAMATION = 8;
        int PARSING_STRING_LITERAL = 9;
    }

    private final InputStream mStream;
    private int mLine = 1;
    private int mColumn = 1;
    private int mTokenStartLine;
    private int mTokenStartColumn;
    private int mState = State.WAITING_FOR_TOKEN;
    private StringBuilder mBuilder;
    private int mRemainingCh = -1;

    public TokenParser(InputStream stream) {
        ensureNonNull(stream);
        mStream = stream;
    }

    private static boolean isValidWhiteSpace(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static boolean isValidTokenCharacter(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9';
    }

    private void throwSyntaxError(String message) throws SyntaxErrorException {
        throw new SyntaxErrorException(message, mLine, mColumn);
    }

    public Token next() throws IOException, SyntaxErrorException {
        int ch = (mRemainingCh >= 0)? mRemainingCh : mStream.read();
        mRemainingCh = -1;

        Token result = null;
        if (ch >= 0) {
            while (result == null) {
                if (mState == State.WAITING_FOR_TOKEN) {
                    if (isValidTokenCharacter(ch)) {
                        mBuilder = new StringBuilder();
                        mBuilder.append((char) ch);
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.PARSING_TOKEN;
                    }
                    else if (ch == '=') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_EQUALS;
                    }
                    else if (ch == '-') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_MINUS;
                    }
                    else if (ch == '/') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_SLASH;
                    }
                    else if (ch == '>') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_GREATER_THAN;
                    }
                    else if (ch == '<') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_LOWER_THAN;
                    }
                    else if (ch == '!') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_EXCLAMATION;
                    }
                    else if (ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == '+' || ch == '*' || ch == '%' || ch == '&' || ch == '|' || ch == ';' || ch == ':' || ch == ',') {
                        result = new Token(mLine, mColumn, "" + ((char) ch));
                    }
                    else if (ch == '"') {
                        mBuilder = new StringBuilder();
                        mBuilder.append((char) ch);
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.PARSING_STRING_LITERAL;
                    }
                    else if (!isValidWhiteSpace(ch)) {
                        throwSyntaxError("Unexpected character");
                    }
                }
                else if (mState == State.PARSING_TOKEN) {
                    if (isValidTokenCharacter(ch)) {
                        mBuilder.append((char) ch);
                    }
                    else if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, mBuilder.toString());
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '+' || ch == '%' || ch == ':' || ch == ',' || ch == ';') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, mBuilder.toString());
                        mRemainingCh = ch;
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (ch == '=') {
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_EQUALS;
                    }
                    else if (ch == '.') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, mBuilder.toString());
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.LAST_WAS_DOT;
                    }
                    else {
                        throwSyntaxError("Unexpected character");
                    }
                }
                else if (mState == State.LAST_WAS_EQUALS) {
                    if (ch == '=') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "==");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "=");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected '=' or whitespace after '='");
                    }
                }
                else if (mState == State.LAST_WAS_DOT) {
                    if (ch == '.') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "..");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (isValidTokenCharacter(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, ".");
                        mBuilder = new StringBuilder();
                        mBuilder.append((char) ch);
                        mTokenStartLine = mLine;
                        mTokenStartColumn = mColumn;
                        mState = State.PARSING_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected '.' after '.'");
                    }
                }
                else if (mState == State.LAST_WAS_MINUS) {
                    if (ch == '>') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "->");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "-");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected '>' after '-'");
                    }
                }
                else if (mState == State.LAST_WAS_SLASH) {
                    if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "/");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected whitespace after '/'");
                    }
                }
                else if (mState == State.LAST_WAS_GREATER_THAN) {
                    if (ch == '=') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, ">=");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, ">");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected whitespace after '>'");
                    }
                }
                else if (mState == State.LAST_WAS_LOWER_THAN) {
                    if (ch == '=') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "<=");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else if (isValidWhiteSpace(ch)) {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "<");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected whitespace after '<'");
                    }
                }
                else if (mState == State.LAST_WAS_EXCLAMATION) {
                    if (ch == '=') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, "!=");
                        mState = State.WAITING_FOR_TOKEN;
                    }
                    else {
                        throwSyntaxError("Expected '=' after '!'");
                    }
                }
                else if (mState == State.PARSING_STRING_LITERAL) {
                    mBuilder.append((char) ch);
                    if (ch == '"') {
                        result = new Token(mTokenStartLine, mTokenStartColumn, mBuilder.toString());
                        mState = State.WAITING_FOR_TOKEN;
                    }
                }

                if (mRemainingCh == -1) {
                    if (ch == '\n') {
                        mLine++;
                        mColumn = 1;
                    }
                    else {
                        mColumn++;
                    }
                }

                if (result == null) {
                    ch = mStream.read();
                }
            }
        }

        return result;
    }
}
