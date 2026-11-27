package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaLambdaExpression implements JavaExpression {
    private final ImmutableList<String> mParamNames;
    private final JavaExpression mBody;

    public JavaLambdaExpression(ImmutableList<String> paramNames, JavaExpression body) {
        ensureNonNull(paramNames, body);
        ensureValidArguments(!paramNames.isEmpty());
        mParamNames = paramNames;
        mBody = body;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return ((mParamNames.size() == 1)? mParamNames.valueAt(0) : "(" + mParamNames.reduce((a, b) -> a + ", " + b) + ")") +
                " -> " + mBody.getText(importedClassReferences);
    }
}
