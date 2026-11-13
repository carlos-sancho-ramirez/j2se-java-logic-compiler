package sword.logic.compiler.generator.c.expressions;

import sword.collections.ImmutableList;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class CFunctionExecutionExpression implements CExpression {
    private final CExpression mFunction;
    private final ImmutableList<CExpression> mParameters;

    public CFunctionExecutionExpression(CExpression function, ImmutableList<CExpression> parameters) {
        ensureNonNull(function, parameters);
        mFunction = function;
        mParameters = parameters;
    }

    @Override
    public String getText() {
        return mFunction.getText() + "(" + mParameters.map(CExpression::getText).reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
