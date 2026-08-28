package sword.logic.interpreter.type.definitions;

import sword.logic.types.Type;

/**
 * Represents anything that can be used as type for constants, function parameters or register fields.
 */
public interface TypeMention extends TypeDefinition {
    Type untokenize();
}
