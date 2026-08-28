package sword.logic.interpreter.expressions;

import sword.collections.ImmutableList;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ArrayConstructionExpression implements Expression {
    private final Token mType;
    private final ImmutableList<Expression> mParameters;

    public ArrayConstructionExpression(Token type, ImmutableList<Expression> parameters) {
        ensureValidArguments(type.getText() == TypeConstants.ARRAY_TYPE_TEXT);
        mType = type;
        mParameters = parameters;
    }
}
