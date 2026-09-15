package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.CFunction;
import sword.logic.compiler.generator.c.Formatter;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CFunctionDefinitionStatement implements CFileRootStatement {
    private final CFunction mFunction;

    public CFunctionDefinitionStatement(CFunction function) {
        ensureNonNull(function);
        mFunction = function;
    }

    @Override
    public String getText(Formatter formatter) {
        String result = (mFunction.isStatic()? "static " : "") + mFunction.getSignature() + " {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (CStatement statement : mFunction.getBody()) {
            result += statement.getText(innerFormatter) + "\n";
        }

        return result + "}";
    }
}
