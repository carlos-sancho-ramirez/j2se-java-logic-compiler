package sword.logic.interpreter.expressions;

import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.ImpossibleSituationException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.types.ArrayType;
import sword.logic.types.EmptyArrayType;
import sword.logic.types.IntType;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.expressions.RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class RegisterFieldAccessExpression implements Expression {
    private final Token mDotOperator;
    private final Expression mRegister;
    private final Token mFieldName;

    public RegisterFieldAccessExpression(Token dotOperator, Expression register, Token fieldName) {
        ensureValidArguments(dotOperator.getText().equals("."));
        ensureNonNull(register);
        ensureValidArguments(validConstantName(fieldName.getText()));
        mDotOperator = dotOperator;
        mRegister = register;
        mFieldName = fieldName;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mRegister.findAllFinders(outMap, scope);
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return mRegister.dependencies();
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        final Type rawType = resolvedExpressions.get(mRegister, null);
        if (rawType != null) {
            if (rawType instanceof RegisterType regType) {
                final ImmutableMap<String, Type> fields = regType.getFields();
                final String fieldNameText = mFieldName.getText();
                if (fields.containsKey(fieldNameText)) {
                    return fields.get(fieldNameText);
                }
                else {
                    throw new SemanticErrorException("Unknown field name", mFieldName.getLine(), mFieldName.getColumn());
                }
            }
            else if (rawType instanceof ArrayType arrayType) {
                if (mFieldName.getText().equals(ARRAY_FIELD_LENGTH)) {
                    return arrayType.getLengthType();
                }
                else {
                    throw new SemanticErrorException("Invalid field name within array. Only 'length' is allowed here", mDotOperator.getLine(), mDotOperator.getColumn());
                }
            }
            else {
                throw new SemanticErrorException("Expected register type", mDotOperator.getLine(), mDotOperator.getColumn());
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        final Type rawType = mRegister.resolveType(restrictionMap);
        if (rawType instanceof RegisterType regType) {
            return regType.getFields().get(mFieldName.getText());
        }
        else {
            ensureValidState(rawType instanceof ArrayType);
            ensureValidState(mFieldName.getText().equals(ARRAY_FIELD_LENGTH));
            return ((ArrayType) rawType).getLengthType();
        }
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) throws ImpossibleSituationException {
        final Type rawRegType = resolvedExpressions.get(mRegister, null);
        final Type rawThisType = resolvedExpressions.get(this, null);
        if (rawRegType == null || rawThisType == null) {
            return null;
        }
        else if (rawRegType instanceof ArrayType arrayType && resultType instanceof IntType resultIntType && mFieldName.getText().equals(ARRAY_FIELD_LENGTH)) {
            final Type newArrayType;
            final IntType validLengthResultType = resultIntType.getIntersection(IntType.getAnyNonNegativeIntType());
            if (validLengthResultType.getRanges().first().getMin().equals(TypeConstants.zeroText) && validLengthResultType.getRanges().last().getMax().equals(TypeConstants.zeroText)) {
                newArrayType = EmptyArrayType.getInstance();
            }
            else {
                newArrayType = arrayType.getLengthType().equals(validLengthResultType)? arrayType :
                        new ArrayType(validLengthResultType, arrayType.getItemType());
            }

            return mRegister.restrictionMap(newArrayType, resolvedExpressions, restrictionMap);
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    @Override
    public sword.logic.expressions.RegisterFieldAccessExpression untokenize(ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap, Map<Expression, Type> resolvedExpressions) {
        return new sword.logic.expressions.RegisterFieldAccessExpression(mRegister.untokenize(typeAliasResolverMap, resolvedExpressions), mFieldName.getText());
    }
}
