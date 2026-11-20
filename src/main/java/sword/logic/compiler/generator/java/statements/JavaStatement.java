package sword.logic.compiler.generator.java.statements;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.entities.JavaWildcardableClassReference;

public interface JavaStatement {
    String getText(ImmutableSet<JavaWildcardableClassReference> importedClassReferences, Formatter formatter);
}
