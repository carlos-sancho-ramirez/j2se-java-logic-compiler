package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.expressions.CExpression;
import sword.logic.compiler.generator.c.types.CTypeDeclaration;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CConstantDefinitionStatement implements CFileRootStatement {
    private final String mName;
    private final CTypeDeclaration mType;
    private final CExpression mValue;

    public CConstantDefinitionStatement(String name, CTypeDeclaration type, CExpression value) {
        ensureNonNull(name, type, value);
        mName = name;
        mType = type;
        mValue = value;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mType.getText() + " " + mName + " = " + mValue.getText() + ";";
    }
}
