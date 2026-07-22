package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.Map;
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
    private final Type mResultingType;
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
        else if (values.get(0).resultingType() instanceof IntegerType firstItemType) {
            IntegerType itemType = firstItemType;
            for (int i = 1; i < values.size(); i++) {
                if (values.get(i).resultingType() instanceof IntegerType thisItemType) {
                    itemType = itemType.getUnion(thisItemType);
                }
                else {
                    throw new UnsupportedOperationException("Unable to mix integers and " + values.get(i).resultingType().getClass().getName() + " in a single array");
                }
            }

            final Token lengthToken = new Token("" + mValues.size());
            mResultingType = new ArrayType(new IntegerType(lengthToken, lengthToken), itemType);
        }
        else if (values.get(0).resultingType() instanceof ArrayType firstItemType) {
            if (firstItemType.getItemType() instanceof IntegerType firstItemItemType) {
                String itemLengthMinText = firstItemType.getLengthType().getMin().getText();
                String itemLengthMaxText = firstItemType.getLengthType().getMax().getText();
                IntegerType itemItemType = firstItemItemType;
                for (int i = 1; i < values.size(); i++) {
                    if (values.get(i).resultingType() instanceof ArrayType thisItemType) {
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
                        throw new UnsupportedOperationException("Unable to mix arrays and " + values.get(i).resultingType().getClass().getName() + " in a single array");
                    }
                }

                final Token lengthToken = new Token("" + values.size());
                final Token itemLengthMaxToken = itemLengthMinText.equals(TypeConstants.unboundText)? TypeConstants.unboundToken : new Token(itemLengthMaxText);
                final IntegerType itemLengthType = new IntegerType(new Token(itemLengthMinText), itemLengthMaxToken);
                mResultingType = new ArrayType(new IntegerType(lengthToken, lengthToken), new ArrayType(itemLengthType, itemItemType));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented for array of other types than integers");
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented for items of type " + values.get(0).resultingType() + " at " + type.getLine() + ":" + type.getColumn());
        }
    }

    public ImmutableList<Expression> getValues() {
        return mValues;
    }

    @Override
    public Type resultingType() {
        return mResultingType;
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance()) {
            return this;
        }
        else if (type instanceof ArrayType arrayType) {
            final ImmutableList<Expression> newValues = mapThrowing(mValues, v -> v.resultTo(arrayType.getItemType()));
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
}
