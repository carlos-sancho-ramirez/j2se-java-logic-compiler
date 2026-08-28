package sword.logic.interpreter.types;

import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayTypeDefinition implements TypeMention {
    private final Token mType;
    private final TypeMention mItemType;

    public ArrayTypeDefinition(Token type, TypeMention itemType) {
        ensureValidArguments(type.getText() == TypeConstants.ARRAY_TYPE_TEXT);
        ensureNonNull(itemType);
        mType = type;
        mItemType = itemType;
    }
}
