package sword.logic.compiler.generator.c.expressions;

import sword.logic.compiler.generator.c.types.CTypeDeclaration;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CSizeofExpression implements CExpression {
    private final CTypeDeclaration mCType;

    public CSizeofExpression(CTypeDeclaration cType) {
        ensureNonNull(cType);
        mCType = cType;
    }

    @Override
    public String getText() {
        return "sizeof(" + mCType.getText() + ")";
    }
}
