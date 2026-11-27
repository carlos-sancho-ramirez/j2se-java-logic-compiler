package sword.logic.compiler.generator.java.types;

import sword.collections.ImmutableSet;
import sword.logic.compiler.generator.java.entities.JavaImportableReference;

public interface JavaType {
    /**
     * Return the string representation that should be displayed in the Java source file.
     * @param importedClassReferences Set of imported class references within the file.
     *                                If this type is a class reference, this parameter
     *                                can be use to avoid adding the whole qualified
     *                                name if it is already imported.
     * @return The text that should be displayed in the resulting Java source code for this type.
     */
    String getTypeText(ImmutableSet<JavaImportableReference> importedClassReferences);

    /**
     * Return the character used in the Java Native Interface (JNI) for the type.
     * In case of being an object, just L will be returned.
     */
    String getSignatureType();
}
