package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaClassReference;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;
import sword.logic.compiler.generator.java.entities.JavaPackage;
import sword.logic.compiler.generator.java.entities.JavaWildcardReference;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class JavaStaticMethodExecutionExpression implements JavaExpression {
    private final JavaClassReference mClassReference;
    private final String mMethodName;
    private final ImmutableList<JavaExpression> mParameters;

    public JavaStaticMethodExecutionExpression(JavaClassReference classReference, String methodName, ImmutableList<JavaExpression> parameters) {
        ensureNonNull(classReference, methodName, parameters);
        mClassReference = classReference;
        mMethodName = methodName;
        mParameters = parameters;
    }

    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        final JavaPackage javaPackage = mClassReference.getPackage();
        final boolean simpleNameIsOk = importedClassReferences.anyMatch(ref -> ref == mClassReference || ref instanceof JavaWildcardReference wRef && wRef.getPackage().equals(javaPackage));
        final String className = mClassReference.getName();
        return (simpleNameIsOk? className : javaPackage.toStringList().append(className).reduce((a, b) -> a + '.' + b) + ".") +
                mMethodName + "(" + mParameters.map(p -> p.getText(importedClassReferences)).reduce((a, b) -> a + ", " + b, "") + ")";
    }
}
