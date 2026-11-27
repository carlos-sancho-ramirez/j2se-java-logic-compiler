package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaMethodExecutionExpression implements JavaExpression {
    private final JavaExpression mInstanceExpression;
    private final String mMethodName;
    private final ImmutableList<JavaExpression> mParameters;

    public JavaMethodExecutionExpression(JavaExpression instanceExpression, String methodName, ImmutableList<JavaExpression> parameters) {
        ensureNonNull(instanceExpression, methodName, parameters);
        mInstanceExpression = instanceExpression;
        mMethodName = methodName;
        mParameters = parameters;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return mInstanceExpression.getText(importedClassReferences) + "." + mMethodName + "(" +
                mParameters.map(p -> p.getText(importedClassReferences)).reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
