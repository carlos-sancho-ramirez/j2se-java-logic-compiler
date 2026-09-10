package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

// So far we do not allow backslashes, we reserve this for escaping characters
public final class CCharLiteralExpression implements CExpression {
    private final String mLiteralNoQuoted;

    public CCharLiteralExpression(String literalNoQuoted) {
        ensureValidArguments(literalNoQuoted.length() == 1 && literalNoQuoted.charAt(0) != '\\');
        mLiteralNoQuoted = literalNoQuoted;
    }

    @Override
    public String getText() {
        return "'" + mLiteralNoQuoted + "'";
    }
}
