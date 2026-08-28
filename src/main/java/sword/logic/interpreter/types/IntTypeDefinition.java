package sword.logic.interpreter.types;

import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class IntTypeDefinition implements TypeMention {
    private final Token mType;
    private final Token mMin;
    private final Token mMax;

    public IntTypeDefinition(Token type, Token min, Token max) {
        ensureValidArguments(type.getText() == TypeConstants.INTEGER_TYPE_TEXT);
        ensureValidArguments(min.getText().equals(TypeConstants.unboundText) || max.getText().equals(TypeConstants.unboundText) || IntegerLiteralOperations.lowerOrEqualThan(min.getText(), max.getText()));
        mType = type;
        mMin = min;
        mMax = max;
    }
}
