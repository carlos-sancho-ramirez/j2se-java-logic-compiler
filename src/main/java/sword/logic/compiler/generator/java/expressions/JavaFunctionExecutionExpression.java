package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaFunctionExecutionExpression implements JavaExpression {
    private final String mFunctionMame; // TODO: This should be changed to a JavaMethodReference instead
    private final ImmutableList<JavaExpression> mParameters;

    public JavaFunctionExecutionExpression(String functionName, ImmutableList<JavaExpression> parameters) {
        ensureNonNull(functionName, parameters);
        mFunctionMame = functionName;
        mParameters = parameters;
    }

    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return mFunctionMame + "(" + mParameters.map(p -> p.getText(importedClassReferences)).reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
