package sword.logic.compiler.generator.c;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class DefaultFormatter implements Formatter {
    public static final String INDENTATION = "  ";
    private final String mIndentation;

    private DefaultFormatter(String indentation) {
        ensureNonNull(indentation);
        mIndentation = indentation;
    }

    public DefaultFormatter() {
        this("");
    }

    @Override
    public String getIndentation() {
        return mIndentation;
    }

    @Override
    public Formatter increaseIndentation() {
        return new DefaultFormatter(mIndentation + INDENTATION);
    }
}
