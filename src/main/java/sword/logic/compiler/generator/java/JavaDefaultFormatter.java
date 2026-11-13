package sword.logic.compiler.generator.java;

import sword.logic.compiler.generator.Formatter;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaDefaultFormatter implements Formatter {
    public static final String INDENTATION = "    ";
    private final String mIndentation;

    private JavaDefaultFormatter(String indentation) {
        ensureNonNull(indentation);
        mIndentation = indentation;
    }

    public JavaDefaultFormatter() {
        this("");
    }

    @Override
    public String getIndentation() {
        return mIndentation;
    }

    @Override
    public Formatter increaseIndentation() {
        return new JavaDefaultFormatter(mIndentation + INDENTATION);
    }
}
