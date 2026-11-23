package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public interface JavaExpression {
    String getText(ImmutableSet<JavaImportableReference> importedClassReferences);
}
