package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.Map;
import sword.collections.Procedure;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.ArrayType;
import sword.logic.syntax_tree.types.IntegerType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.TypeConstants;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.collections.ImmutableListExtensions.mapThrowing;
import static sword.logic.compiler.PreconditionUtils.ensureNonNull;

public final class ArrayConstructor implements Expression {
    private final Type mRequiredType;
    private final Token mType;
    private final ImmutableList<Expression> mValues;

    public ArrayConstructor(Token type, ImmutableList<Expression> values) {
        ensureNonNull(type, values);
        mType = type;
        mValues = values;

        if (values.isEmpty()) {
            // TODO: Deal with the possibility of having no values... should be UnknownType then???
            throw new UnsupportedOperationException("Unimplemented");
        }
        else if (values.get(0).requiredType() instanceof IntegerType firstItemType) {
            IntegerType itemType = firstItemType;
            for (int i = 1; i < values.size(); i++) {
                if (values.get(i).requiredType() instanceof IntegerType thisItemType) {
                    itemType = itemType.getUnion(thisItemType);
                }
                else {
                    throw new UnsupportedOperationException("Unable to mix integers and " + values.get(i).requiredType().getClass().getName() + " in a single array");
                }
            }

            final Token lengthToken = new Token("" + mValues.size());
            mRequiredType = new ArrayType(new IntegerType(lengthToken, lengthToken), itemType);
        }
        else if (values.get(0).requiredType() instanceof ArrayType firstItemType) {
            if (firstItemType.getItemType() instanceof IntegerType firstItemItemType) {
                String itemLengthMinText = firstItemType.getLengthType().getMin().getText();
                String itemLengthMaxText = firstItemType.getLengthType().getMax().getText();
                IntegerType itemItemType = firstItemItemType;
                for (int i = 1; i < values.size(); i++) {
                    if (values.get(i).requiredType() instanceof ArrayType thisItemType) {
                        itemLengthMinText = IntegerLiteralOperations.min(itemLengthMinText, thisItemType.getLengthType().getMin().getText());
                        if (!itemLengthMaxText.equals(TypeConstants.unboundText)) {
                            final String thisItemLengthMaxText = thisItemType.getLengthType().getMax().getText();
                            itemLengthMaxText = thisItemLengthMaxText.equals(TypeConstants.unboundText)? TypeConstants.unboundText :
                                    IntegerLiteralOperations.max(itemLengthMaxText, thisItemLengthMaxText);
                        }

                        if (thisItemType.getItemType() instanceof IntegerType thisItemItemType) {
                            itemItemType = itemItemType.getUnion(thisItemItemType);
                        }
                        else {
                            throw new UnsupportedOperationException("Unable to mix arrays of of different types within an array");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unable to mix arrays and " + values.get(i).requiredType().getClass().getName() + " in a single array");
                    }
                }

                final Token lengthToken = new Token("" + values.size());
                final Token itemLengthMaxToken = itemLengthMinText.equals(TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(itemLengthMaxText);
                final IntegerType itemLengthType = new IntegerType(new Token(itemLengthMinText), itemLengthMaxToken);
                mRequiredType = new ArrayType(new IntegerType(lengthToken, lengthToken), new ArrayType(itemLengthType, itemItemType));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented for array of other types than integers");
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented for items of type " + values.get(0).requiredType() + " at " + type.getLine() + ":" + type.getColumn());
        }
    }

    public ImmutableList<Expression> getValues() {
        return mValues;
    }

    @Override
    public Type requiredType() {
        return mRequiredType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof ArrayType arrayType) {
            final ImmutableList<Expression> newValues = mapThrowing(mValues, v -> v.requiresType(arrayType.getItemType()));
            return (newValues == mValues)? this : new ArrayConstructor(mType, newValues);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to an Integer. It is already an Array");
        }
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        for (Expression value : mValues) {
            value.resolveReferences(knownTargets);
        }
    }

    @Override
    public Type resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        if (mValues.isEmpty()) {
            return new ArrayType(new IntegerType(TypeConstants.zeroToken, TypeConstants.zeroToken), UnknownType.getInstance());
        }
        else if (mValues.get(0).resultingType(paramTypes, logger) instanceof IntegerType firstItemType) {
            IntegerType itemType = firstItemType;
            for (int i = 1; i < mValues.size(); i++) {
                if (mValues.get(i).resultingType(paramTypes, logger) instanceof IntegerType thisItemType) {
                    itemType = itemType.getUnion(thisItemType);
                }
                else {
                    throw new UnsupportedOperationException("Unable to mix integers and " + mValues.get(i).resultingType(paramTypes, logger).getClass().getName() + " in a single array");
                }
            }

            final Token lengthToken = new Token("" + mValues.size());
            return new ArrayType(new IntegerType(lengthToken, lengthToken), itemType);
        }
        else if (mValues.get(0).resultingType(paramTypes, logger) instanceof ArrayType firstItemType) {
            if (firstItemType.getItemType() instanceof IntegerType firstItemItemType) {
                String itemLengthMinText = firstItemType.getLengthType().getMin().getText();
                String itemLengthMaxText = firstItemType.getLengthType().getMax().getText();
                IntegerType itemItemType = firstItemItemType;
                for (int i = 1; i < mValues.size(); i++) {
                    if (mValues.get(i).resultingType(paramTypes, logger) instanceof ArrayType thisItemType) {
                        itemLengthMinText = IntegerLiteralOperations.min(itemLengthMinText, thisItemType.getLengthType().getMin().getText());
                        if (!itemLengthMaxText.equals(TypeConstants.unboundText)) {
                            final String thisItemLengthMaxText = thisItemType.getLengthType().getMax().getText();
                            itemLengthMaxText = thisItemLengthMaxText.equals(TypeConstants.unboundText)? TypeConstants.unboundText :
                                    IntegerLiteralOperations.max(itemLengthMaxText, thisItemLengthMaxText);
                        }

                        if (thisItemType.getItemType() instanceof IntegerType thisItemItemType) {
                            itemItemType = itemItemType.getUnion(thisItemItemType);
                        }
                        else {
                            throw new UnsupportedOperationException("Unable to mix arrays of of different types within an array");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unable to mix arrays and " + mValues.get(i).resultingType(paramTypes, logger).getClass().getName() + " in a single array");
                    }
                }

                final Token lengthToken = new Token("" + mValues.size());
                final Token itemLengthMaxToken = itemLengthMinText.equals(TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(itemLengthMaxText);
                final IntegerType itemLengthType = new IntegerType(new Token(itemLengthMinText), itemLengthMaxToken);
                return new ArrayType(new IntegerType(lengthToken, lengthToken), new ArrayType(itemLengthType, itemItemType));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented for array of other types than integers");
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented for items of type " + mValues.get(0).resultingType(paramTypes, logger) + " at " + mType.getLine() + ":" + mType.getColumn());
        }
    }
}
