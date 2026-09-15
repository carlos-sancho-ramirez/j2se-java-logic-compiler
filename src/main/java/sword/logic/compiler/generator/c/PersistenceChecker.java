package sword.logic.compiler.generator.c;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableMap;
import sword.logic.expressions.ArrayConcatenationExpression;
import sword.logic.expressions.ArrayConstructionExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.expressions.FunctionExecutionExpression;
import sword.logic.expressions.IfExpression;
import sword.logic.expressions.LeftRightExpression;
import sword.logic.expressions.LiteralExpression;
import sword.logic.expressions.ReferenceExpression;
import sword.logic.expressions.RegisterConstructionExpression;
import sword.logic.expressions.RegisterFieldAccessExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.expressions.RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH;

final class PersistenceChecker {
    private final ImmutableMap<Expression, Type> mTypeMap;
    private final ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> mFunctionMap;
    private final MutableMap<KeyPair, ImmutableMap<String, Persistence>> mResultMap = MutableHashMap.empty();

    PersistenceChecker(
            ImmutableMap<Expression, Type> typeMap,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap) {
        ensureNonNull(typeMap, functionMap);
        mTypeMap = typeMap;
        mFunctionMap = functionMap;
    }

    /**
     * Instances of this interface are assigned to function parameters to
     * determine how much of that parameter is still potentially persisting
     * in the result of the function.
     * This is relevant to know which parts of each parameter must be kept in
     * memory after calling the method.
     * <p>
     * This is only relevant for Array and Register type parameters, which are
     * the only ones dealing internally with C structs. Other types like
     * integers, whose values will be copied, will never be considered persisted.
     */
    interface Persistence {
    }

    /**
     * Used to confirm that a param is fully persisted, and then all memory
     * allocated for it must be kept after the function call.
     */
    static final Persistence fullPersistence = new Persistence() {
    };

    /**
     * Used only for array type parameters to determine that the Array struct
     * is not needed after the method call, but the values inside are still
     * present in the result.
     */
    static final Persistence arrayValuesPersistence = new Persistence() {
    };

    /**
     * Used only for array type parameters to determine that only some items
     * inside the array will be persisted, but the Array struct and its
     * values array inside are not needed after calling this method.
     */
    static final class ArrayItemPersistence implements Persistence {
        private final Persistence mItemPersistence;

        private ArrayItemPersistence(Persistence itemPersistence) {
            ensureNonNull(itemPersistence);
            mItemPersistence = itemPersistence;
        }

        Persistence getItemPersistence() {
            return mItemPersistence;
        }
    }

    /**
     * Used only for register type parameters to determine the persistence of each of the fields.
     */
    static final class FieldsPersistence implements Persistence {
        private final ImmutableMap<String, Persistence> mFieldPersistenceMap;

        private FieldsPersistence(ImmutableMap<String, Persistence> fieldPersistenceMap) {
            ensureValidArguments(!fieldPersistenceMap.isEmpty());
            mFieldPersistenceMap = fieldPersistenceMap;
        }

        ImmutableMap<String, Persistence> getFieldPersistenceMap() {
            return mFieldPersistenceMap;
        }
    }

    interface InPersistence {
        Persistence buildOutput();
    }

    final InPersistence full = new InPersistence() {
        @Override
        public Persistence buildOutput() {
            return fullPersistence;
        }
    };

    final InPersistence arrayValuesInPersistence = new InPersistence() {
        @Override
        public Persistence buildOutput() {
            return arrayValuesPersistence;
        }
    };

    final InPersistence arrayConcatenationInPersistence = new InPersistence() {
        @Override
        public Persistence buildOutput() {
            return new ArrayItemPersistence(fullPersistence);
        }
    };

    static final class ArrayItemInPersistence implements InPersistence {
        private final InPersistence mWrapped;

        ArrayItemInPersistence(InPersistence wrapped) {
            ensureNonNull(wrapped);
            mWrapped = wrapped;
        }

        InPersistence getWrapped() {
            return mWrapped;
        }

        @Override
        public Persistence buildOutput() {
            return new ArrayItemPersistence(mWrapped.buildOutput());
        }
    }

    static final class FieldInPersistence implements InPersistence {
        private final InPersistence mWrapped;
        private final String mFieldName;

        FieldInPersistence(InPersistence wrapped, String fieldName) {
            ensureNonNull(wrapped, fieldName);
            mWrapped = wrapped;
            mFieldName = fieldName;
        }

        @Override
        public FieldsPersistence buildOutput() {
            return new FieldsPersistence(new ImmutableHashMap.Builder<String, Persistence>()
                    .put(mFieldName, mWrapped.buildOutput())
                    .build());
        }
    }

    static Persistence getUnionOfPersistences(Persistence a, Persistence b) {
        return (a == fullPersistence || b == fullPersistence)? fullPersistence :
                (a == arrayValuesPersistence || b == arrayValuesPersistence)? arrayValuesPersistence :
                (a instanceof ArrayItemPersistence aItem && b instanceof ArrayItemPersistence bItem)? new ArrayItemPersistence(getUnionOfPersistences(aItem.getItemPersistence(), bItem.getItemPersistence())) :
                new FieldsPersistence(getUnionOfPersistenceMaps(((FieldsPersistence) a).getFieldPersistenceMap(), ((FieldsPersistence) b).getFieldPersistenceMap()));
    }

    private static ImmutableMap<String, Persistence> getUnionOfPersistenceMaps(ImmutableMap<String, Persistence> a, ImmutableMap<String, Persistence> b) {
        return a.keySet().addAll(b.keySet()).assign(key ->
                (!a.containsKey(key))? b.get(key) :
                b.containsKey(key)? getUnionOfPersistences(a.get(key), b.get(key)) : a.get(key));
    }

    InPersistence toInPersistence(Persistence persistence) {
        if (persistence == fullPersistence) {
            return full;
        }
        else if (persistence == arrayValuesPersistence) {
            return arrayValuesInPersistence;
        }
        else if (persistence instanceof ArrayItemPersistence pers) {
            return new ArrayItemInPersistence(toInPersistence(pers.getItemPersistence()));
        }
        else {
            ensureValidState(persistence instanceof FieldsPersistence);
            return full; // TODO: Fix this when relevant
        }
    }

    private ImmutableMap<String, Persistence> workOutExpressionPersistence(
            ImmutableList<String> spaceName,
            Expression expression,
            InPersistence inPersistence) {
        if (expression instanceof ArrayConcatenationExpression exp) {
            final ImmutableMap<String, Persistence> leftPersistences = obtainExpressionPersistence(spaceName, exp.getLeftExpression(), arrayConcatenationInPersistence);
            final ImmutableMap<String, Persistence> rightPersistences = obtainExpressionPersistence(spaceName, exp.getRightExpression(), arrayConcatenationInPersistence);
            return getUnionOfPersistenceMaps(leftPersistences, rightPersistences);
        }
        else if (expression instanceof ArrayConstructionExpression exp) {
            if (inPersistence instanceof FieldInPersistence fieldPers) {
                ensureValidState(fieldPers.mFieldName.equals(ARRAY_FIELD_LENGTH));
                return ImmutableHashMap.empty();
            }
            else {
                final InPersistence newInPersistence = (inPersistence instanceof ArrayItemInPersistence arrayPers)? arrayPers.mWrapped : full;
                return exp.getValues()
                        .map(v -> obtainExpressionPersistence(spaceName, v, newInPersistence))
                        .reduce(PersistenceChecker::getUnionOfPersistenceMaps, ImmutableHashMap.empty());
            }
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            // We do not persist anything from the index... should we check it anyway? If so, which InPersistence should I provide???
            return obtainExpressionPersistence(spaceName, exp.getArray(), new ArrayItemInPersistence(inPersistence));
        }
        else if (expression instanceof ComplexExpression exp) {
            final ImmutableSet<ConstantDefinitionStatement> constDefs = exp.getStatements()
                    .filter(st -> st instanceof ConstantDefinitionStatement)
                    .map(st -> (ConstantDefinitionStatement) st)
                    .toSet();
            final ImmutableSet<String> localConstantNames = constDefs
                    .map(Statement::getName)
                    .toSet();
            final ImmutableMap<String, ConstantDefinitionStatement> constDefsMap = localConstantNames.assign(name ->
                    constDefs.findFirst(st -> st.getName().equals(name), null));
            ImmutableMap<String, Persistence> flatPersistences = obtainExpressionPersistence(spaceName, exp.getExpression(), inPersistence);

            final MutableMap<String, Persistence> alreadyAccumulated = MutableHashMap.empty();
            while (flatPersistences.keySet().anyMatch(localConstantNames::contains)) {
                final int targetIndex = flatPersistences.keySet().indexWhere(localConstantNames::contains);
                final String target = flatPersistences.keyAt(targetIndex);
                final PersistenceChecker.Persistence oldPersistence = flatPersistences.valueAt(targetIndex);
                flatPersistences = flatPersistences.removeAt(targetIndex);

                if (alreadyAccumulated.containsKey(target)) {
                    if (!alreadyAccumulated.get(target).equals(oldPersistence)) {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    alreadyAccumulated.put(target, oldPersistence);

                    final ImmutableMap<String, PersistenceChecker.Persistence> statementPersistenceMap = obtainStatementPersistence(spaceName, constDefsMap.get(target), toInPersistence(oldPersistence));
                    for (Map.Entry<String, PersistenceChecker.Persistence> entry : statementPersistenceMap.entries()) {
                        final PersistenceChecker.Persistence newPersistence;
                        if (flatPersistences.containsKey(entry.key())) {
                            newPersistence = getUnionOfPersistences(flatPersistences.get(entry.key()), entry.value());
                        }
                        else {
                            newPersistence = entry.value();
                        }

                        flatPersistences = flatPersistences.put(entry.key(), newPersistence);
                    }
                }
            }

            return flatPersistences;
        }
        else if (expression instanceof FunctionDefinitionExpression exp) {
            obtainExpressionPersistence(spaceName, exp.getBody(), full);
            return ImmutableHashMap.empty();
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            if (exp.getFunction() instanceof ReferenceExpression refExp) {
                final String functionName = refExp.getReference();
                ImmutableList<String> targetSpaceName = spaceName;
                FunctionDefinitionExpression tempFuncDefExp = null;
                do {
                    tempFuncDefExp = mFunctionMap.get(targetSpaceName.append(functionName), null);
                    if (tempFuncDefExp == null) {
                        ensureValidState(!targetSpaceName.isEmpty());
                        targetSpaceName = targetSpaceName.skipLast(1);
                    }
                }
                while (tempFuncDefExp == null);

                final FunctionDefinitionExpression funcDefExp = tempFuncDefExp;
                ensureValidState(exp.getParameters().size() == funcDefExp.getParameters().size());

                final ImmutableMap<String, Persistence> funcPersistence = obtainExpressionPersistence(targetSpaceName.append(functionName), funcDefExp.getBody(), full);
                return exp.getParameters().indexes().map(paramIndex -> {
                    final String paramName = funcDefExp.getParameters().valueAt(paramIndex).getName();
                    final ImmutableMap<String, Persistence> result = funcPersistence.containsKey(paramName)?
                        obtainExpressionPersistence(spaceName, exp.getParameters().valueAt(paramIndex), toInPersistence(funcPersistence.get(paramName))) : ImmutableHashMap.empty();
                    return result;
                }).reduce(PersistenceChecker::getUnionOfPersistenceMaps, ImmutableHashMap.empty());
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof IfExpression exp) {
            // We do not persist anything from the condition... should we check it anyway? If so, which InPersistence should I provide???
            final ImmutableMap<String, Persistence> thenPersistence = obtainExpressionPersistence(spaceName, exp.getThenClause(), inPersistence);
            final ImmutableMap<String, Persistence> elsePersistence = obtainExpressionPersistence(spaceName, exp.getElseClause(), inPersistence);
            return getUnionOfPersistenceMaps(thenPersistence, elsePersistence);
        }
        else if (expression instanceof LeftRightExpression) {
            // The only LeftRightExpression that should not return empty is the ArrayConcatenation,
            // the rest results in integer or boolean values, which are never referenced but copied.
            // Logic for ArrayConcatenation is above in this if chain.
            return ImmutableHashMap.empty();
        }
        else if (expression instanceof LiteralExpression) {
            return ImmutableHashMap.empty();
        }
        else if (expression instanceof ReferenceExpression exp) {
            return new ImmutableHashMap.Builder<String, Persistence>()
                    .put(exp.getReference(), inPersistence.buildOutput())
                    .build();
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            return exp.getStatements()
                    .map(st -> obtainStatementPersistence(spaceName, st, full))
                    .reduce(PersistenceChecker::getUnionOfPersistenceMaps, ImmutableHashMap.empty());
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            final Type fieldType = mTypeMap.get(exp);
            if (fieldType instanceof RegisterType) {
                return obtainExpressionPersistence(spaceName, exp.getRegister(), new FieldInPersistence(inPersistence, exp.getFieldName()));
            }
            else {
                return ImmutableHashMap.empty();
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    ImmutableMap<String, Persistence> obtainExpressionPersistence(
            ImmutableList<String> spaceName,
            Expression expression,
            InPersistence inPersistence) {
        final KeyPair newKeyPair = new KeyPair(expression, inPersistence);
        final ImmutableMap<String, Persistence> cachedResult = mResultMap.get(newKeyPair, null);
        if (cachedResult != null) {
            return cachedResult;
        }
        else {
            final ImmutableMap<String, Persistence> newResult = workOutExpressionPersistence(spaceName, expression, inPersistence);
            mResultMap.put(newKeyPair, newResult);
            return newResult;
        }
    }

    ImmutableMap<String, Persistence> obtainStatementPersistence(
            ImmutableList<String> spaceName,
            Statement statement,
            InPersistence inPersistence) {
        return (statement instanceof ConstantDefinitionStatement constDef)?
                obtainExpressionPersistence(spaceName.append(constDef.getName()), constDef.getExpression(), inPersistence) : ImmutableHashMap.empty();
    }

    ImmutableMap<KeyPair, ImmutableMap<String, Persistence>> check(ImmutableList<Statement> statements) {
        for (Statement statement : statements) {
            obtainStatementPersistence(ImmutableList.empty(), statement, full);
        }

        return mResultMap.toImmutable();
    }
}
