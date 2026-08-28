package sword.logic.interpreter.type.definitions;

import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.types.Type;

/**
 * Represents anything that is allowed to be written on the right side of the equals token in a type definition.
 */
public interface TypeDefinition {
    /**
     * Returns a {@link Type} from the current definition.
     * <p>
     * This method will never return null.
     *
     * @param resolver Used to resolve any type alias found.
     * @return The type for this definition.
     * @throws UnresolvedTypeReferenceException In case of having a type alias that cannot be resolved.
     */
    Type resolve(TypeAliasResolver resolver) throws UnresolvedTypeReferenceException;
}
