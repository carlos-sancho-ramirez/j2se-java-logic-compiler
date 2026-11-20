package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

public final class JavaNewExpression implements JavaExpression {
    // TODO: Fix this once we have constructors
    @Override
    public String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences) {
        return "new MyClass()";
    }
}
