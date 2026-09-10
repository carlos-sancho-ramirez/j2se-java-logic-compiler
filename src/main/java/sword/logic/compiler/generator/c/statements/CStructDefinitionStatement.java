package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.c.CVariable;
import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.types.CStructType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CStructDefinitionStatement implements CFileRootStatement {
    private final CStructType mStruct;

    public CStructDefinitionStatement(CStructType struct) {
        ensureNonNull(struct);
        mStruct = struct;
    }

    @Override
    public String getText(Formatter formatter) {
        String result = formatter.getIndentation() + mStruct.getText() + " {\n";
        final Formatter innerFormatter = formatter.increaseIndentation();
        for (CVariable field : mStruct.getFields()) {
            result += innerFormatter.getIndentation() + field.getTypeDeclaration().getText() + " " + field.getName() + ";\n";
        }

        return result + formatter.getIndentation() + "};";
    }
}
