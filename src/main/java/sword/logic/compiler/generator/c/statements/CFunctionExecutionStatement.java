package sword.logic.compiler.generator.c.statements;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.Formatter;
import sword.logic.compiler.generator.c.expressions.CExpression;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CFunctionExecutionStatement implements CInFunctionStatement {
    private final CExpression mFunction;
    private final ImmutableList<CExpression> mParameters;

    public CFunctionExecutionStatement(CExpression function, ImmutableList<CExpression> parameters) {
        ensureNonNull(function, parameters);
        mFunction = function;
        mParameters = parameters;
    }

    @Override
    public String getText(Formatter formatter) {
        return formatter.getIndentation() + mFunction.getText() + "(" + mParameters.map(CExpression::getText).reduce((a, b) -> a + ", " + b, "") + ");";
    }
}
