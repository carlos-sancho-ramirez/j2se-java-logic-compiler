package sword.logic.compiler.generator.c;

import sword.collections.ImmutableIntList;
import sword.collections.ImmutableIntValueHashMap;
import sword.collections.ImmutableIntValueMap;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.IntValueMap;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableMap;
import sword.logic.expressions.ArrayConcatenationExpression;
import sword.logic.expressions.ArrayConstructionExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
import sword.logic.expressions.EnumValueLiteralExpression;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.expressions.FunctionExecutionExpression;
import sword.logic.expressions.IfExpression;
import sword.logic.expressions.IntegerLiteralExpression;
import sword.logic.expressions.LeftRightExpression;
import sword.logic.expressions.ReferenceExpression;
import sword.logic.expressions.RegisterConstructionExpression;
import sword.logic.expressions.RegisterFieldAccessExpression;
import sword.logic.expressions.StringLiteralExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;
import sword.logic.types.ArrayType;
import sword.logic.types.FunctionParameter;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;
import sword.logic.types.TypeConstants;

import java.util.Objects;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.compiler.generator.c.PersistenceChecker.fullPersistence;
import static sword.logic.compiler.generator.c.PersistenceChecker.getUnionOfPersistences;

final class RequiredAllocationsChecker {
    private final ImmutableMap<Expression, Type> mTypeMap;
    private final ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> mFunctionMap;
    private final PersistenceChecker mPersistenceChecker;
    private final Result mEmptyResult = new Result(ImmutableIntValueHashMap.empty(), ImmutableIntList.empty());
    private final MutableMap<KeyPair, Result> mResultMap = MutableHashMap.empty();

    RequiredAllocationsChecker(
            ImmutableMap<Expression, Type> typeMap,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker) {
        ensureNonNull(typeMap, functionMap, persistenceChecker);
        mTypeMap = typeMap;
        mFunctionMap = functionMap;
        mPersistenceChecker = persistenceChecker;
    }

    private ImmutableIntList getUnionOfPointersInArray(ImmutableIntList a, ImmutableIntList b) {
        ImmutableIntList result = ImmutableIntList.empty();
        boolean somethingChanged;
        do {
            somethingChanged = false;
            for (int i = 0; i < a.size(); i++) {
                final int value = a.valueAt(i);
                final int index = b.indexOf(value);
                if (index >= 0) {
                    a = a.removeAt(i);
                    b = b.removeAt(index);
                    result = result.append(value);
                    somethingChanged = true;
                    break;
                }
            }
        }
        while (somethingChanged);

        while (!a.isEmpty() && !b.isEmpty()) {
            final int aMin = a.min();
            final int bMin = b.min();

            a = a.removeAt(a.indexOf(aMin));
            b = b.removeAt(b.indexOf(bMin));
            result = result.append(Math.max(aMin, bMin));
        }

        return result.appendAll(a).appendAll(b);
    }

    private Result checkExpression(
            ImmutableList<String> spaceName,
            Expression expression,
            PersistenceChecker.InPersistence inPersistence) {
        if (expression instanceof ArrayConcatenationExpression exp) {
            final Result leftResult = obtainCheckedExpression(spaceName, exp.getLeftExpression(), mPersistenceChecker.arrayConcatenationInPersistence);
            final Result rightResult = obtainCheckedExpression(spaceName, exp.getRightExpression(), mPersistenceChecker.arrayConcatenationInPersistence);
            final ImmutableIntValueMap<RegisterType.Definition> additionalStructs = leftResult.mStructs.keySet().addAll(rightResult.mStructs.keySet()).assignToInt(k ->
                    leftResult.mStructs.get(k, 0) + rightResult.mStructs.get(k, 0));
            final String max = ((ArrayType) mTypeMap.get(exp)).getLengthType().getMax();
            if (max.equals(TypeConstants.unboundText)) {
                // This would require dynamic memory allocation...
                throw new UnsupportedOperationException("Unimplemented");
            }

            final ImmutableIntList newPointersInArray = new ImmutableIntList.Builder()
                    .append(Integer.parseInt(max))
                    .build();
            return new Result(additionalStructs, newPointersInArray);
        }
        else if (expression instanceof ArrayConstructionExpression exp) {
            final PersistenceChecker.InPersistence valuesInPersistence = (inPersistence instanceof PersistenceChecker.ArrayItemInPersistence arrayPers)? arrayPers.getWrapped() : mPersistenceChecker.full;
            final ImmutableList<Result> valuesResult = exp.getValues()
                    .map(v -> obtainCheckedExpression(spaceName, v, valuesInPersistence));
            final ImmutableIntValueMap<RegisterType.Definition> newStructCount = valuesResult
                    .map(r -> r.mStructs)
                    .reduce((a, b) -> a.keySet().addAll(b.keySet()).assignToInt(k -> a.get(k, 0) + b.get(k, 0)), ImmutableIntValueHashMap.empty());
            final ImmutableIntList newPointersInArray = valuesResult
                    .map(Result::getPointersInArray)
                    .reduce(ImmutableIntList::appendAll, ImmutableIntList.empty())
                    .append(exp.getValues().size());
            return new Result(newStructCount, newPointersInArray);
        }
        else if (expression instanceof ArrayValueAtExpression) {
            return mEmptyResult;
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
            final ImmutableMap<String, PersistenceChecker.Persistence> expressionPersistences = mPersistenceChecker.obtainExpressionPersistence(spaceName, exp.getExpression(), inPersistence);
            final Result expressionResult = obtainCheckedExpression(spaceName, exp.getExpression(), inPersistence);

            ImmutableIntValueMap<RegisterType.Definition> additionalStructs = expressionResult.getStructs();
            ImmutableIntList pointersInArray = expressionResult.getPointersInArray();

            ImmutableMap<String, PersistenceChecker.Persistence> flatPersistences = expressionPersistences;
            final MutableMap<String, PersistenceChecker.Persistence> alreadyAccumulated = MutableHashMap.empty();
            while (flatPersistences.keySet().anyMatch(localConstantNames::contains)) {
                final int targetIndex = flatPersistences.keySet().indexWhere(localConstantNames::contains);
                final String target = flatPersistences.keyAt(targetIndex);
                final PersistenceChecker.Persistence oldPersistence = flatPersistences.valueAt(targetIndex);
                final ImmutableMap<String, PersistenceChecker.Persistence> statementPersistenceMap = mPersistenceChecker.obtainStatementPersistence(spaceName, constDefsMap.get(target), mPersistenceChecker.toInPersistence(oldPersistence));
                flatPersistences = flatPersistences.removeAt(targetIndex);

                if (alreadyAccumulated.containsKey(target)) {
                    if (!alreadyAccumulated.get(target).equals(oldPersistence)) {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
                else {
                    alreadyAccumulated.put(target, oldPersistence);
                    final Result statementAllocations = obtainCheckedStatement(spaceName, constDefsMap.get(target), mPersistenceChecker.toInPersistence(oldPersistence));

                    if (oldPersistence == fullPersistence) {
                        for (IntValueMap.Entry<RegisterType.Definition> entry : statementAllocations.getStructs().entries()) {
                            additionalStructs = additionalStructs.put(entry.key(), entry.value() + additionalStructs.get(entry.key(), 0));
                        }
                        pointersInArray = pointersInArray.appendAll(statementAllocations.getPointersInArray());

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
                    else if (oldPersistence instanceof PersistenceChecker.ArrayItemPersistence) {
                        for (IntValueMap.Entry<RegisterType.Definition> entry : statementAllocations.getStructs().entries()) {
                            additionalStructs = additionalStructs.put(entry.key(), entry.value() + additionalStructs.get(entry.key(), 0));
                        }

                        final int maxPointers = Integer.parseInt(((ArrayType) mTypeMap.get(constDefsMap.get(target).getExpression())).getLengthType().getMax());
                        final int index = statementAllocations.getPointersInArray().indexOf(maxPointers);
                        pointersInArray = pointersInArray.appendAll((index >= 0)? statementAllocations.getPointersInArray().removeAt(index) : statementAllocations.getPointersInArray());

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
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }
            }

            return new Result(additionalStructs, pointersInArray);
        }
        else if (expression instanceof EnumValueLiteralExpression) {
            return mEmptyResult;
        }
        else if (expression instanceof FunctionDefinitionExpression exp) {
            obtainCheckedExpression(spaceName, exp.getBody(), inPersistence);
            return mEmptyResult;
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
                final ImmutableList<String> funcQualifiedName = targetSpaceName.append(functionName);
                ensureValidState(exp.getParameters().size() == funcDefExp.getParameters().size());

                final ImmutableMap<String, PersistenceChecker.Persistence> funcPersistences = mPersistenceChecker.obtainExpressionPersistence(funcQualifiedName, funcDefExp.getBody(), inPersistence);
                final ImmutableMap<String, PersistenceChecker.Persistence> paramPersistences = funcPersistences.filterByKey(funcDefExp.getParameters().map(FunctionParameter::getName)::contains);
                ImmutableIntList pointersInArray = ImmutableIntList.empty();

                for (int paramPersistenceIndex : paramPersistences.indexes()) {
                    final String paramName = paramPersistences.keyAt(paramPersistenceIndex);
                    final PersistenceChecker.Persistence paramPersistence = paramPersistences.valueAt(paramPersistenceIndex);
                    final int paramIndex = funcDefExp.getParameters().indexWhere(p -> p.getName().equals(paramName));
                    final Expression paramExp = exp.getParameters().valueAt(paramIndex);
                    if (paramPersistence == fullPersistence) {
                        if (paramExp instanceof StringLiteralExpression litExp) {
                            pointersInArray = pointersInArray.append(litExp.getArrayLength());
                        }
                        else {
                            final Result paramResult = obtainCheckedExpression(spaceName, paramExp, inPersistence);
                            if (!paramResult.getStructs().isEmpty()) {
                                throw new UnsupportedOperationException("Unimplemented");
                            }

                            pointersInArray = pointersInArray.appendAll(paramResult.getPointersInArray());
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }

                final Result funcResult = obtainCheckedExpression(funcQualifiedName, funcDefExp.getBody(), inPersistence);
                return new Result(funcResult.getStructs(), funcResult.getPointersInArray().appendAll(pointersInArray));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof IfExpression exp) {
            final Result thenResult = obtainCheckedExpression(spaceName, exp.getThenClause(), inPersistence);
            final Result elseResult = obtainCheckedExpression(spaceName, exp.getElseClause(), inPersistence);

            final ImmutableIntValueMap<RegisterType.Definition> additionalStructs = thenResult.mStructs.keySet().addAll(elseResult.mStructs.keySet()).assignToInt(k ->
                    Math.max(thenResult.mStructs.get(k, 0), elseResult.mStructs.get(k, 0)));
            return new Result(additionalStructs, getUnionOfPointersInArray(thenResult.getPointersInArray(), elseResult.getPointersInArray()));
        }
        else if (expression instanceof IntegerLiteralExpression) {
            return mEmptyResult;
        }
        else if (expression instanceof LeftRightExpression) {
            // The only LeftRightExpression that should not return empty is the ArrayConcatenation,
            // the rest results in integer or boolean values, which are never referenced but copied.
            // Logic for ArrayConcatenation is above in this if chain.
            return mEmptyResult;
        }
        else if (expression instanceof ReferenceExpression) {
            return mEmptyResult;
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            final RegisterType regType = (RegisterType) mTypeMap.get(exp);
            final RegisterType.Definition regDef = regType.getDefinition();
            final ImmutableList<Result> results = exp.getStatements().map(st -> obtainCheckedStatement(spaceName, st, inPersistence));
            final ImmutableIntValueMap<RegisterType.Definition> structsForOutput = results
                    .map(r -> r.mStructs)
                    .reduce((a, b) -> a.keySet().addAll(b.keySet()).assignToInt(k -> a.get(k, 0) + b.get(k, 0)));
            return new Result(
                    structsForOutput.put(regDef, structsForOutput.get(regDef, 0) + 1),
                    results.map(Result::getPointersInArray).reduce(ImmutableIntList::appendAll, ImmutableIntList.empty()));
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            final Type fieldType = mTypeMap.get(exp);
            if (fieldType instanceof RegisterType) {
                return obtainCheckedExpression(spaceName, exp.getRegister(), new PersistenceChecker.FieldInPersistence(inPersistence, exp.getFieldName()));
            }
            else {
                return mEmptyResult;
            }
        }
        else if (expression instanceof StringLiteralExpression exp) {
            if (inPersistence == mPersistenceChecker.arrayConcatenationInPersistence || inPersistence instanceof PersistenceChecker.ArrayItemInPersistence) {
                return mEmptyResult;
            }
            else {
                final ImmutableIntList pointersInArray = new ImmutableIntList.Builder()
                        .append(exp.getArrayLength())
                        .build();
                return new Result(ImmutableIntValueHashMap.empty(), pointersInArray);
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    Result obtainCheckedExpression(
            ImmutableList<String> spaceName,
            Expression expression,
            PersistenceChecker.InPersistence inPersistence) {
        final KeyPair newKeyPair = new KeyPair(expression, inPersistence);
        final Result cachedResult = mResultMap.get(newKeyPair, null);
        if (cachedResult != null) {
            return cachedResult;
        }
        else {
            final Result newResult = checkExpression(spaceName, expression, inPersistence);
            mResultMap.put(newKeyPair, newResult);
            return newResult;
        }
    }

    private Result obtainCheckedStatement(
            ImmutableList<String> spaceName,
            Statement statement,
            PersistenceChecker.InPersistence inPersistence) {
        return (statement instanceof ConstantDefinitionStatement constDef)?
                obtainCheckedExpression(spaceName.append(constDef.getName()), constDef.getExpression(), inPersistence) : mEmptyResult;
    }

    ImmutableMap<KeyPair, Result> check(ImmutableList<Statement> statements) {
        for (Statement statement : statements) {
            obtainCheckedStatement(ImmutableList.empty(), statement, mPersistenceChecker.full);
        }

        return mResultMap.toImmutable();
    }

    static final class Result {
        private final ImmutableIntValueMap<RegisterType.Definition> mStructs;
        private final ImmutableIntList mPointersInArray;

        private Result(
                ImmutableIntValueMap<RegisterType.Definition> structs,
                ImmutableIntList pointersInArrays) {
            ensureValidArguments(structs.keySet().allMatch(Objects::nonNull) && structs.allMatch(v -> v > 0));
            ensureValidArguments(pointersInArrays.allMatch(v -> v >= 0));
            mStructs = structs;
            mPointersInArray = pointersInArrays;
        }

        /**
         * Return the map assigning to each register type the number of instances that should be allocated
         */
        public ImmutableIntValueMap<RegisterType.Definition> getStructs() {
            return mStructs;
        }

        /**
         * Return how many array structs should be allocated and how many pointers should be allocated for each inside.
         */
        public ImmutableIntList getPointersInArray() {
            return mPointersInArray;
        }
    }
}
