package sword.logic.interpreter.scopes;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableTransformable;
import sword.collections.Map;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.statements.Statement;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.IntType;
import sword.logic.types.Type;

/**
 * Base scope that any program witten in this language will use.
 * <p>
 * This contains implicit definitions of the language, like the Boolean type.
 */
public final class BuiltInScope extends AbstractScope {
    private static BuiltInScope mInstance;

    public static BuiltInScope getInstance() {
        if (mInstance == null) {
            mInstance = new BuiltInScope();
        }

        return mInstance;
    }

    private final EnumType mBooleanType = new EnumType(new ImmutableHashSet.Builder<String>()
            .add(TypeConstants.BOOLEAN_VALUE_TRUE)
            .add(TypeConstants.BOOLEAN_VALUE_FALSE)
            .build());

    private final ArrayType mStringType = new ArrayType(
            new IntType(TypeConstants.zeroText, TypeConstants.unboundText),
            new IntType(TypeConstants.zeroText, "255"));

    private final EnumType mAlwaysTrueType = new EnumType(new ImmutableHashSet.Builder<String>()
            .add(TypeConstants.BOOLEAN_VALUE_TRUE)
            .build());

    private final EnumType mAlwaysFalseType = new EnumType(new ImmutableHashSet.Builder<String>()
            .add(TypeConstants.BOOLEAN_VALUE_FALSE)
            .build());

    public EnumType getBooleanType() {
        return mBooleanType;
    }

    public ArrayType getStringType() {
        return mStringType;
    }

    public EnumType getAlwaysTrueType() {
        return mAlwaysTrueType;
    }

    public EnumType getAlwaysFalseType() {
        return mAlwaysFalseType;
    }

    @Override
    public boolean knowsConstantName(String constantName) {
        return false;
    }

    @Override
    public Type resolveTypeAlias(Token typeAlias) throws UnresolvedTypeReferenceException {
        if (typeAlias.getText().equals(TypeConstants.BOOLEAN_TYPE_TEXT)) {
            return mBooleanType;
        }
        else if (typeAlias.getText().equals(TypeConstants.STRING_TYPE_TEXT)) {
            return mStringType;
        }
        else {
            throw new UnresolvedTypeReferenceException(typeAlias);
        }
    }

    @Override
    Type resolveDefinedType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedReferenceException {
        throw new UnresolvedReferenceException(constantName);
    }

    @Override
    Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws UnresolvedReferenceException {
        throw new UnresolvedReferenceException(constantName);
    }

    @Override
    public Type resolveType(Token constantName, Map<Expression, Type> resolvedExpressions) throws UnresolvedReferenceException {
        throw new UnresolvedReferenceException(constantName);
    }

    public Scope createWithStatements(ImmutableTransformable<Statement> statements) {
        return new StatementsHolderScope(this, statements.toSet());
    }

    private BuiltInScope() {
    }
}
