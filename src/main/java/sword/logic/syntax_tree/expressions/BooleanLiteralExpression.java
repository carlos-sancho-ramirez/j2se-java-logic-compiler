package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class BooleanLiteralExpression implements Expression {
    private final Token mToken;
    private final boolean mValue;

    public BooleanLiteralExpression(Token token, boolean value) {
        ensureNonNull(token);
        mToken = token;
        mValue = value;
    }

    public boolean getValue() {
        return mValue;
    }

    @Override
    public Type requiredType() {
        return TypeConstants.booleanType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type == TypeConstants.booleanType) {
            return this;
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ". It is already a Boolean");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        // Nothing to resolve
    }
}
