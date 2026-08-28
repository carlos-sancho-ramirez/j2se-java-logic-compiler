package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.expressions.EnumValueLiteralExpression;
import sword.logic.expressions.IntegerLiteralExpression;
import sword.logic.expressions.StringLiteralExpression;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.scopes.BuiltInScope;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.ArrayType;
import sword.logic.types.EmptyArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.types.EnumType.validEnumValueName;

public final class LiteralExpression implements Expression {
    private final Token mLiteral;

    public LiteralExpression(Token literal) {
        // TODO: Check all valid literals
        ensureValidArguments(validEnumValueName(literal.getText()) ||
                literal.getText().charAt(0) == '"' && literal.getText().charAt(literal.getText().length() - 1) == '"' ||
                IntegerLiteralOperations.validIntegerLiteral(literal.getText()));
        mLiteral = literal;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return ImmutableHashSet.empty();
    }

    private Type resolveTypeInternal(Scope scope) throws UnresolvedEnumValueException {
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
        else if (validEnumValueName(literalText)) {
            final EnumType enumType = scope.resolveEnumValue(mLiteral);
            return new EnumType(enumType.getDefinition(), new ImmutableHashSet.Builder<String>()
                    .add(literalText)
                    .build());
        }
        // TODO: Return proper type for single quote
        else {
            return new IntType(literalText, literalText);
        }
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedEnumValueException {
        return resolveTypeInternal(scope);
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        // This is fine for now because the developer cannot define custom enum types, so the only ones existing are in the BuiltInScope
        // TODO: This should not depend on the BuiltInScope in order to resolve other enum types than Boolean
        try {
            return resolveTypeInternal(BuiltInScope.getInstance());
        }
        catch (UnresolvedEnumValueException e) {
            throw new RuntimeException("Unable to resolve type", e);
        }
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) {
        return ImmutableHashMap.empty();
    }

    @Override
    public sword.logic.expressions.Expression untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap) {
        final String text = mLiteral.getText();
        final sword.logic.expressions.Expression result = IntegerLiteralOperations.validIntegerLiteral(text)? new IntegerLiteralExpression(text) :
                (text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"')? new StringLiteralExpression(text) :
                new EnumValueLiteralExpression(text);
        outExpressionMap.put(this, result);
        return result;
    }
}
