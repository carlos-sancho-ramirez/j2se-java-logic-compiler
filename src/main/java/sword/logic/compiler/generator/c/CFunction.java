package sword.logic.compiler.generator.c;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.statements.CInFunctionStatement;
import sword.logic.compiler.generator.c.types.CTypeDeclaration;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CFunction {
    private final String mName;
    private final ImmutableList<CVariable> mParams;
    private final CTypeDeclaration mReturnType;
    private final ImmutableList<CInFunctionStatement> mBody;
    private final boolean mIsStatic;

    public CFunction(String name, ImmutableList<CVariable> params, CTypeDeclaration returnType, ImmutableList<CInFunctionStatement> body, boolean isStatic) {
        ensureValidArguments(!name.isEmpty() && params != null && returnType != null);
        ensureValidArguments(params.map(CVariable::getName).toSet().size() == params.size());
        mName = name;
        mParams = params;
        mReturnType = returnType;
        mBody = body;
        mIsStatic = isStatic;
    }

    public String getName() {
        return mName;
    }

    public ImmutableList<CVariable> getParams() {
        return mParams;
    }

    public CTypeDeclaration getReturnType() {
        return mReturnType;
    }

    public ImmutableList<CInFunctionStatement> getBody() {
        return mBody;
    }

    public boolean isStatic() {
        return mIsStatic;
    }

    public String getSignature() {
        return mReturnType.getText() + " " + mName + "(" + mParams.map(param -> param.getTypeDeclaration().getText() + " " + param.getName()).reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
