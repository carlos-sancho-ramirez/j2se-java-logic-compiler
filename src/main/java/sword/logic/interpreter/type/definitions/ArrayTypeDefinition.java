package sword.logic.interpreter.type.definitions;

import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.IntType;

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

    @Override
    public ArrayType resolve(TypeAliasResolver resolver) throws UnresolvedTypeReferenceException {
        return new ArrayType(new IntType(TypeConstants.zeroText, TypeConstants.unboundText), mItemType.resolve(resolver));
    }

    public ArrayType untokenize() {
        return new ArrayType(new IntType(TypeConstants.zeroText, TypeConstants.unboundText), mItemType.untokenize());
    }
}
