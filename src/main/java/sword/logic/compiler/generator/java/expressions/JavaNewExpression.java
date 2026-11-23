package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public final class JavaNewExpression implements JavaExpression {
    // TODO: Fix this once we have constructors
    @Override
    public String getText(ImmutableSet<JavaImportableReference> importedClassReferences) {
        return "new MyClass()";
    }
}
