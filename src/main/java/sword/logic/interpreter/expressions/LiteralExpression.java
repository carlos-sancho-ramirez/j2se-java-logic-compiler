package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.syntax_tree.Token;
import sword.logic.types.ArrayType;
import sword.logic.types.EmptyArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class LiteralExpression implements Expression {
    private final Token mLiteral;

    public LiteralExpression(Token literal) {
        // TODO: Check all valid literals
        ensureValidArguments(literal.getText().charAt(0) >= 'A' && literal.getText().charAt(0) <= 'Z' ||
                literal.getText().charAt(0) == '"' && literal.getText().charAt(literal.getText().length() - 1) == '"' ||
                IntegerLiteralOperations.validIntegerLiteral(literal.getText()));
        mLiteral = literal;
    }

    @Override
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return ImmutableHashSet.empty();
    }

    private Type resolveTypeInternal() {
        final String literalText = mLiteral.getText();
        if (literalText.charAt(0) == '"') {
            final int literalTextLength = literalText.length();

            if (literalTextLength > 2) {
                final String lengthText = "" + (literalTextLength - 2);
                final IntType lengthType = new IntType(lengthText, lengthText);

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

                final IntType itemType = new IntType("" + min, "" + max);
                return new ArrayType(lengthType, itemType);
            }
            else {
                return EmptyArrayType.getInstance();
            }
        }
        else if (literalText.charAt(0) >= 'A' && literalText.charAt(0) <= 'Z') {
            return new EnumType(new ImmutableHashSet.Builder<String>()
                    .add(literalText)
                    .build());
        }
        // TODO: Return proper type for single quote
        else {
            return new IntType(literalText, literalText);
        }
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) {
        return resolveTypeInternal();
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        return resolveTypeInternal();
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) {
        return ImmutableHashMap.empty();
    }
}
