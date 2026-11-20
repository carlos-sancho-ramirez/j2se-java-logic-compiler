package sword.logic.compiler.generator.java.expressions;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

public interface JavaExpression {
    String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences);
}
