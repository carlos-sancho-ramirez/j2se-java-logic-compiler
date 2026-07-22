package sword.logic.syntax_tree.expressions;

import sword.collections.Map;
import sword.collections.Procedure;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class StringLiteralExpression implements Expression {
    private ArrayType mResultingType;
    private final Token mLiteral;

    public StringLiteralExpression(Token literal) {
        ensureValidArguments(literal.getText().charAt(0) == '"');
        mLiteral = literal;
    }

    public Token getLiteral() {
        return mLiteral;
    }

    @Override
    public Type requiredType() {
        if (mResultingType == null) {
            final String text = mLiteral.getText();
            final int textLength = text.length();
            if (textLength > 2) {
                int min = text.charAt(1);
                int max = min;
                for (int i = 2; i < textLength - 1; i++) {
                    int v = text.charAt(i);
                    if (v < min) {
                        min = v;
                    }
                    else if (v > max) {
                        max = v;
                    }
                }

                final Token lengthToken = new Token("" + (textLength - 2));
                mResultingType = new ArrayType(new IntegerType(lengthToken, lengthToken), new IntegerType(new Token("" + min), new Token("" + max)));
            }
            else {
                // Let's leave it as 0 to 127... but in theory it is irrelevant
                final Token lengthToken = new Token("0");
                mResultingType = new ArrayType(new IntegerType(lengthToken, lengthToken), new IntegerType(TypeConstants.zeroToken, new Token("127")));
            }
        }

        return mResultingType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof ArrayType arrayType) {
            if (arrayType.getItemType() == UnknownType.getInstance() || arrayType.getItemType() instanceof IntegerType) {
                return this;
            }
            else {
                throw new TypeMismatchException("Unable to resolve this expression to " + type + ". It is already an Array[Int]");
            }
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type + ". It is already an Array[Int]");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        // Nothing to resolve
    }

    @Override
    public ArrayType resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        final String literalText = mLiteral.getText();
        final int literalTextLength = literalText.length();
        final Token lengthToken = new Token("" + (literalTextLength - 2));
        final IntegerType lengthType = new IntegerType(lengthToken, lengthToken);

        final IntegerType itemType;
        if (literalTextLength > 2) {
            int min = literalText.charAt(1);
            int max = min;
            for (int i = 2; i < literalTextLength - 1; i++) {
                final int v = literalText.charAt(i);
                if (v < min) {
                    min = v;
                }

                if (v > max) {
                    max = v;
                }
            }

            itemType = new IntegerType(new Token("" + min), new Token("" + max));
        }
        else {
            // In theory, the itemType should be ignored if length is zero... but just in case we set a potential value between 0 and 127
            itemType = new IntegerType(TypeConstants.zeroToken, new Token("127"));
        }

        return new ArrayType(lengthType, itemType);
    }
}
