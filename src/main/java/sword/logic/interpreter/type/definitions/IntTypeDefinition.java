package sword.logic.interpreter.type.definitions;

import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.interpreter.Token;
import sword.logic.types.IntType;
import sword.logic.types.TypeConstants;

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

    @Override
    public IntType resolve(TypeAliasResolver resolver) {
        return new IntType(mMin.getText(), mMax.getText());
    }

    public IntType untokenize() {
        return new IntType(mMin.getText(), mMax.getText());
    }
}
