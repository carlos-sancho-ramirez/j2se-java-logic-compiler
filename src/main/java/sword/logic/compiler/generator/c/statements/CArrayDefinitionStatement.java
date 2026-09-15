package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.CVariable;
import sword.logic.compiler.generator.c.Formatter;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CArrayDefinitionStatement implements CInFunctionStatement {
    private final CVariable mVariable;
    private final int mItemCount;

    public CArrayDefinitionStatement(CVariable variable, int itemCount) {
        ensureNonNull(variable);
        mVariable = variable;
        mItemCount = itemCount;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mVariable.getTypeDeclaration().getText() + " " + mVariable.getName() + "[" + mItemCount + "];";
    }
}
