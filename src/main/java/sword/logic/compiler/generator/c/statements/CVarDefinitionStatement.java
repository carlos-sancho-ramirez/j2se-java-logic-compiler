package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.CVariable;
import sword.logic.compiler.generator.c.Formatter;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CVarDefinitionStatement implements CInFunctionStatement {
    private final CVariable mVariable;

    public CVarDefinitionStatement(CVariable variable) {
        ensureNonNull(variable);
        mVariable = variable;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mVariable.getTypeDeclaration().getText() + " " + mVariable.getName() + ";";
    }
}
