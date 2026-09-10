package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.CFunction;
import sword.logic.compiler.generator.c.Formatter;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CFunctionDeclarationStatement implements CFileRootStatement {
    private final CFunction mFunction;

    public CFunctionDeclarationStatement(CFunction function) {
        ensureNonNull(function);
        mFunction = function;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mFunction.getSignature() + ";";
    }
}
