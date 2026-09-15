package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

// So far we do not allow backslashes, we reserve this for escaping characters
public final class CStringLiteralExpression implements CExpression {
    private final String mLiteralNoQuoted;

    public CStringLiteralExpression(String literalNoQuoted) {
        ensureNonNull(literalNoQuoted);
        mLiteralNoQuoted = literalNoQuoted;
    }

    @Override
    public String getText() {
        return "\"" + mLiteralNoQuoted + "\"";
    }
}
