package sword.logic.interpreter.type.definitions;

import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

public interface TypeAliasResolver {
    /**
     * Resolves the type for a given type alias.
     * <p>
     * This method will never return null. In case of error, an exception will be thrown instead.
     * @param typeAlias Alias to be found.
     * @return The real type assigned to the given alias.
     * @throws UnresolvedTypeReferenceException If the alias does not have a proper type definition associated.
     */
    Type resolveTypeAlias(Token typeAlias) throws UnresolvedTypeReferenceException;
}
