package sword.logic.compiler.generator.c;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.IntValueMap;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableHashSet;
import sword.collections.MutableMap;
import sword.collections.MutableSet;
import sword.collections.Set;
import sword.logic.compiler.DefaultVariableNameCreator;
import sword.logic.compiler.VariableNameCreator;
import sword.logic.compiler.generator.c.expressions.CAdditionExpression;
import sword.logic.compiler.generator.c.expressions.CAndExpression;
import sword.logic.compiler.generator.c.expressions.CArrayValueAtExpression;
import sword.logic.compiler.generator.c.expressions.CAssignableExpression;
import sword.logic.compiler.generator.c.expressions.CCastExpression;
import sword.logic.compiler.generator.c.expressions.CDereferenceExpression;
import sword.logic.compiler.generator.c.expressions.CDifferentFromExpression;
import sword.logic.compiler.generator.c.expressions.CDivisionExpression;
import sword.logic.compiler.generator.c.expressions.CEqualThanExpression;
import sword.logic.compiler.generator.c.expressions.CExpression;
import sword.logic.compiler.generator.c.expressions.CGreaterOrEqualThanExpression;
import sword.logic.compiler.generator.c.expressions.CIntLiteralExpression;
import sword.logic.compiler.generator.c.expressions.CLowerThanExpression;
import sword.logic.compiler.generator.c.expressions.CModuleExpression;
import sword.logic.compiler.generator.c.expressions.CMultiplicationExpression;
import sword.logic.compiler.generator.c.expressions.COrExpression;
import sword.logic.compiler.generator.c.expressions.CReferenceExpression;
import sword.logic.compiler.generator.c.expressions.CSizeofExpression;
import sword.logic.compiler.generator.c.expressions.CStringLiteralExpression;
import sword.logic.compiler.generator.c.expressions.CStructFieldAccessExpression;
import sword.logic.compiler.generator.c.expressions.CStructFieldPointerAccessExpression;
import sword.logic.compiler.generator.c.expressions.CSubtractionExpression;
import sword.logic.compiler.generator.c.statements.CAdditionStatement;
import sword.logic.compiler.generator.c.statements.CArrayDefinitionStatement;
import sword.logic.compiler.generator.c.statements.CAssignmentStatement;
import sword.logic.compiler.generator.c.statements.CConstantDefinitionStatement;
import sword.logic.compiler.generator.c.statements.CFileRootStatement;
import sword.logic.compiler.generator.c.statements.CFunctionDeclarationStatement;
import sword.logic.compiler.generator.c.statements.CFunctionDefinitionStatement;
import sword.logic.compiler.generator.c.statements.CFunctionExecutionStatement;
import sword.logic.compiler.generator.c.statements.CIfStatement;
import sword.logic.compiler.generator.c.statements.CInFunctionStatement;
import sword.logic.compiler.generator.c.statements.CStructDefinitionStatement;
import sword.logic.compiler.generator.c.statements.CVarDefinitionStatement;
import sword.logic.compiler.generator.c.types.CCharType;
import sword.logic.compiler.generator.c.types.CIntType;
import sword.logic.compiler.generator.c.types.CPointerType;
import sword.logic.compiler.generator.c.types.CStructDeclarationType;
import sword.logic.compiler.generator.c.types.CStructType;
import sword.logic.compiler.generator.c.types.CTypeDeclaration;
import sword.logic.compiler.generator.c.types.CUnsignedCharType;
import sword.logic.compiler.generator.c.types.CVoidType;
import sword.logic.expressions.AdditionExpression;
import sword.logic.expressions.AndExpression;
import sword.logic.expressions.ArrayConcatenationExpression;
import sword.logic.expressions.ArrayConstructionExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
import sword.logic.expressions.DifferentFromExpression;
import sword.logic.expressions.DivisionExpression;
import sword.logic.expressions.EnumValueLiteralExpression;
import sword.logic.expressions.EqualThanExpression;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.expressions.FunctionExecutionExpression;
import sword.logic.expressions.GreaterOrEqualThanExpression;
import sword.logic.expressions.IfExpression;
import sword.logic.expressions.IntegerLiteralExpression;
import sword.logic.expressions.LeftRightExpression;
import sword.logic.expressions.LiteralExpression;
import sword.logic.expressions.LowerThanExpression;
import sword.logic.expressions.ModuleExpression;
import sword.logic.expressions.MultiplicationExpression;
import sword.logic.expressions.OrExpression;
import sword.logic.expressions.ReferenceExpression;
import sword.logic.expressions.RegisterConstructionExpression;
import sword.logic.expressions.RegisterFieldAccessExpression;
import sword.logic.expressions.StringLiteralExpression;
import sword.logic.expressions.SubtractionExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;
import sword.logic.statements.TypeDefinitionStatement;
import sword.logic.types.ArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.FunctionParameter;
import sword.logic.types.IntType;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;
import sword.logic.types.TypeConstants;

import java.util.Objects;

import static sword.logic.compiler.IntegerLiteralOperations.greaterOrEqualThan;
import static sword.logic.compiler.IntegerLiteralOperations.lowerThan;
import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;
import static sword.logic.compiler.generator.c.StringPoolGenerator.canBeOptimizedInStringPool;
import static sword.logic.expressions.RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH;

public final class CCodeGenerator {
    private static final String ARRAY_FIELD_VALUES = "values";

    private static final String STRING_POOL = "stringPool";
    private static final String OUT_RESULT = "outResult";
    private static final CReferenceExpression OUT_RESULT_REF = new CReferenceExpression(OUT_RESULT);
    private static final String PLACEHOLDER = "<???>";

    private final ImmutableMap<Expression, Type> mTypeMap;
    private final CStructDeclarationType cArrayDeclarationType = new CStructDeclarationType("Array");

    public CCodeGenerator(ImmutableMap<Expression, Type> typeMap) {
        ensureNonNull(typeMap);
        mTypeMap = typeMap;
    }

    private CTypeDeclaration cType(Type type, Map<RegisterType.Definition, RegisterStructTypes> definedStructs) {
        if (type instanceof IntType intType) {
            final String minText = intType.getMin();
            final String maxText = intType.getMax();
            if (minText.equals(TypeConstants.unboundText) || maxText.equals(TypeConstants.unboundText)) {
                return CIntType.getInstance();
            }
            else if (greaterOrEqualThan(minText, "-128") && lowerThan(maxText, "128")) {
                return CCharType.getInstance();
            }
            else if (greaterOrEqualThan(minText, "0") && lowerThan(maxText, "256")) {
                return CUnsignedCharType.getInstance();
            }
            else {
                return CIntType.getInstance();
            }
        }
        else if (type instanceof ArrayType) {
            return cArrayDeclarationType;
        }
        else if (type instanceof EnumType enumType && enumType.isBooleanType()) {
            return CIntType.getInstance();
        }
        else if (type instanceof RegisterType regType) {
            return definedStructs.get(regType.getDefinition()).getDeclaration();
        }
        else {
            return CVoidType.getInstance();
        }
    }

    private boolean areParenthesesRequiredOnMultiplication(Expression expression) {
        return !(expression instanceof IntegerLiteralExpression ||
                expression instanceof ReferenceExpression ||
                expression instanceof MultiplicationExpression ||
                expression instanceof DivisionExpression ||
                expression instanceof ModuleExpression ||
                expression instanceof RegisterFieldAccessExpression ||
                expression instanceof ArrayValueAtExpression ||
                expression instanceof FunctionExecutionExpression);
    }

    private CStructFieldAccessExpression newArrayLengthAccessExpression(CReferenceExpression arrayRef) {
        return new CStructFieldAccessExpression(arrayRef, ARRAY_FIELD_LENGTH);
    }

    private CStructFieldAccessExpression newArrayValuesAccessExpression(CReferenceExpression arrayRef) {
        return new CStructFieldAccessExpression(arrayRef, ARRAY_FIELD_VALUES);
    }

    private CStructFieldPointerAccessExpression newArrayLengthPointerAccessExpression(CReferenceExpression arrayRef) {
        return new CStructFieldPointerAccessExpression(arrayRef, ARRAY_FIELD_LENGTH);
    }

    private CStructFieldPointerAccessExpression newArrayValuesPointerAccessExpression(CReferenceExpression arrayRef) {
        return new CStructFieldPointerAccessExpression(arrayRef, ARRAY_FIELD_VALUES);
    }

    private CExpression traverseExpression(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer) {
        if (expression instanceof AdditionExpression exp) {
            return new CAdditionExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof AndExpression exp) {
            return new CAndExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof ArrayConcatenationExpression exp) {
            final ArrayType arrayType = (ArrayType) mTypeMap.get(exp);
            final IntType lengthType = arrayType.getLengthType();
            final String maxLengthText = lengthType.getMax();
            if (maxLengthText.equals(TypeConstants.unboundText)) {
                throw new UnsupportedOperationException("Unimplemented");
            }
            else {
                final String valuesVarName = varNameCreator.create("values");
                final CReferenceExpression valuesRef = new CReferenceExpression(valuesVarName);
                final int arrayLength = Integer.parseInt(maxLengthText);
                bodyBuilder.append(new CArrayDefinitionStatement(new CVariable(valuesVarName, cType(arrayType.getItemType(), definedStructs)), arrayLength));
                final IntType leftLengthType = ((ArrayType) mTypeMap.get(exp.getLeftExpression())).getLengthType();
                final String leftLengthText = leftLengthType.getMax();
                final String arrayVarName = varNameCreator.create("array");
                bodyBuilder.append(new CVarDefinitionStatement(new CVariable(arrayVarName, cArrayDeclarationType)));
                final CReferenceExpression arrayRef = new CReferenceExpression(arrayVarName);

                if (leftLengthType.getMin().equals(leftLengthText)) {
                    assignExpressionToArrayValues(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, valuesRef, 0);
                    assignExpressionToArrayValues(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, valuesRef, Integer.parseInt(leftLengthText));
                    bodyBuilder.append(new CAssignmentStatement(newArrayLengthAccessExpression(arrayRef), new CIntLiteralExpression("" + arrayLength)));
                    bodyBuilder.append(new CAssignmentStatement(newArrayValuesAccessExpression(arrayRef), new CReferenceExpression(valuesVarName)));
                }
                else {
                    assignExpressionToArray(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, arrayRef, false);
                    final String secondArrayVarName = varNameCreator.create("array");
                    bodyBuilder.append(new CVarDefinitionStatement(new CVariable(secondArrayVarName, cArrayDeclarationType)));
                    final CReferenceExpression secondArrayRef = new CReferenceExpression(secondArrayVarName);

                    bodyBuilder.append(new CAssignmentStatement(newArrayValuesAccessExpression(secondArrayRef), new CAdditionExpression(newArrayValuesAccessExpression(arrayRef), new CMultiplicationExpression(newArrayLengthAccessExpression(arrayRef), new CSizeofExpression(cType(arrayType.getItemType(), definedStructs))))));
                    assignExpressionToArray(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, secondArrayRef, false);
                    bodyBuilder.append(new CAdditionStatement(newArrayLengthAccessExpression(arrayRef), newArrayLengthAccessExpression(secondArrayRef)));
                }

                return new CReferenceExpression(arrayVarName);
            }
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            final CExpression arrayExp = traverseExpression(exp.getArray(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final CExpression indexExp = traverseExpression(exp.getIndex(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final Type resultType = mTypeMap.get(exp);
            final CExpression casted = new CCastExpression(new CPointerType(cType(resultType, definedStructs)), new CArrayValueAtExpression(new CStructFieldPointerAccessExpression(arrayExp, ARRAY_FIELD_VALUES), indexExp));
            return (resultType instanceof ArrayType || resultType instanceof RegisterType)? casted :
                    new CArrayValueAtExpression(casted, new CIntLiteralExpression(TypeConstants.zeroText));
        }
        else if (expression instanceof EnumValueLiteralExpression exp) {
            return new CIntLiteralExpression(TypeConstants.BOOLEAN_VALUE_TRUE.equals(exp.getValue())? "1" : "0");
        }
        else if (expression instanceof ComplexExpression exp) {
            final MutableMap<String, Expression> scopeDefinitions = MutableHashMap.empty();
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof TypeDefinitionStatement typeDef) {
                    traverseTypeDefinitionStatement(typeDef, definedStructs, definedTypes);
                }
            }

            final MutableSet<String> newRefIsPointer = refIsPointer.mutate();
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    final Type statementType = mTypeMap.get(constDef.getExpression());
                    if (statementType instanceof ArrayType || statementType instanceof RegisterType) {
                        newRefIsPointer.add(constDef.getName());
                    }
                }
            }

            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    traverseConstantDefinitionStatement(constDef, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, scopeDefinitions, varNameCreator, stringPool, newRefIsPointer);
                }
            }

            traverseExpression(exp.getExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            throw new UnsupportedOperationException("Unimplemented return");
        }
        else if (expression instanceof DifferentFromExpression exp) {
            return new CDifferentFromExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof DivisionExpression exp) {
            return new CDivisionExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof EqualThanExpression exp) {
            return new CEqualThanExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            for (Expression param : exp.getParameters()) {
                traverseExpression(param, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            }
            traverseExpression(exp.getFunction(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            throw new UnsupportedOperationException("Unimplemented return");
        }
        else if (expression instanceof GreaterOrEqualThanExpression exp) {
            return new CGreaterOrEqualThanExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof IfExpression exp) {
            traverseExpression(exp.getCondition(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            traverseExpression(exp.getThenClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            traverseExpression(exp.getElseClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            throw new UnsupportedOperationException("Unimplemented return");
        }
        else if (expression instanceof IntegerLiteralExpression exp) {
            return new CIntLiteralExpression(exp.getLiteral());
        }
        else if (expression instanceof LowerThanExpression exp) {
            return new CLowerThanExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof ModuleExpression exp) {
            return new CModuleExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof MultiplicationExpression exp) {
            return new CMultiplicationExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof OrExpression exp) {
            return new COrExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else if (expression instanceof ReferenceExpression exp) {
            final String reference = exp.getReference();
            ImmutableList<String> targetSpaceName = spaceName;
            while (!targetSpaceName.isEmpty()) {
                if (functionMap.containsKey(targetSpaceName.append(reference))) {
                    return new CReferenceExpression(targetSpaceName.append(reference).reduce((a, b) -> a + "_" + b));
                }

                targetSpaceName = targetSpaceName.skipLast(1);
            }

            final Expression defConstant = definedConstants.get(exp.getReference(), null);
            if (defConstant instanceof ArrayConstructionExpression arrayConstructionExp && canBeOptimizedInStringPool(arrayConstructionExp)) {
                return new CAdditionExpression(new CReferenceExpression(STRING_POOL), new CIntLiteralExpression("" + stringPool.getIndexes().get((StringLiteralExpression) arrayConstructionExp.getValues().valueAt(0))));
            }
            else {
                return new CReferenceExpression(exp.getReference());
            }
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            final MutableMap<String, Expression> scopeDefinitions = MutableHashMap.empty();
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    traverseConstantDefinitionStatement(constDef, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, scopeDefinitions, varNameCreator, stringPool, refIsPointer);
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            throw new UnsupportedOperationException("Unimplemented return");
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            final CExpression register = traverseExpression(exp.getRegister(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            return new CStructFieldPointerAccessExpression(register, exp.getFieldName());
        }
        else if (expression instanceof StringLiteralExpression exp) {
            final IntType itemType = (IntType) ((ArrayType) mTypeMap.get(exp)).getItemType();
            final String valuesVarName = varNameCreator.create("values");
            final int arrayLength = exp.getArrayLength();
            bodyBuilder.append(new CArrayDefinitionStatement(new CVariable(valuesVarName, new CPointerType(cType(itemType, definedStructs))), arrayLength));
            for (int i = 0; i < arrayLength; i++) {
                final String ch = exp.getCharAt(i);
                final int index = stringPool.getPool().indexOf(ch);
                ensureValidState(index >= 0);
                bodyBuilder.append(new CAssignmentStatement(new CArrayValueAtExpression(new CReferenceExpression(valuesVarName), new CIntLiteralExpression("" + i)), new CAdditionExpression(new CReferenceExpression(STRING_POOL), new CIntLiteralExpression("" + index))));
            }

            final String arrayVarName = varNameCreator.create("array");
            final CReferenceExpression arrayRef = new CReferenceExpression(arrayVarName);
            bodyBuilder.append(new CVarDefinitionStatement(new CVariable(arrayVarName, new CStructDeclarationType("Array"))));
            bodyBuilder.append(new CAssignmentStatement(newArrayLengthAccessExpression(arrayRef), new CIntLiteralExpression("" + arrayLength)));
            bodyBuilder.append(new CAssignmentStatement(newArrayValuesAccessExpression(arrayRef), new CCastExpression(new CPointerType(new CPointerType(CVoidType.getInstance())), new CReferenceExpression(valuesVarName))));

            return new CDereferenceExpression(arrayRef);
        }
        else if (expression instanceof SubtractionExpression exp) {
            return new CSubtractionExpression(traverseExpression(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer),
                    traverseExpression(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
        }
        else {
            throw new UnsupportedOperationException("traverseExpression unimplemented for expression " + expression.getClass().getSimpleName());
        }
    }

    private void assignExpressionToArrayValues(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer,
            CAssignableExpression outArrayValuesRef,
            int outOffset) {
        if (expression instanceof ArrayConcatenationExpression exp) {
            final IntType leftLengthType = ((ArrayType) mTypeMap.get(exp.getLeftExpression())).getLengthType();
            final String maxLeftLengthText = leftLengthType.getMax();
            if (maxLeftLengthText.equals(leftLengthType.getMin())) {
                assignExpressionToArrayValues(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayValuesRef, outOffset);
                assignExpressionToArrayValues(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayValuesRef, outOffset + Integer.parseInt(maxLeftLengthText));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            final CExpression ref = traverseExpression(exp.getArray(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final CExpression index = traverseExpression(exp.getIndex(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            bodyBuilder.append(new CAssignmentStatement(new CArrayValueAtExpression(outArrayValuesRef, new CIntLiteralExpression("" + outOffset)), new CAdditionExpression(ref, index)));
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            if (exp.getFunction() instanceof ReferenceExpression refExp) {
                final String functionName = refExp.getReference();
                ImmutableList<String> targetSpaceName = spaceName;
                FunctionDefinitionExpression tempFuncDefExp;
                do {
                    tempFuncDefExp = functionMap.get(targetSpaceName.append(functionName), null);
                    if (tempFuncDefExp == null) {
                        ensureValidState(!targetSpaceName.isEmpty());
                        targetSpaceName = targetSpaceName.skipLast(1);
                    }
                }
                while (tempFuncDefExp == null);

                final FunctionDefinitionExpression funcDefExp = tempFuncDefExp;
                ensureValidState(exp.getParameters().size() == funcDefExp.getParameters().size());

                ensureValidState(mTypeMap.get(funcDefExp.getBody()) instanceof ArrayType);

                final String tempArrayName = varNameCreator.create("array");
                final CReferenceExpression tempArrayRef = new CReferenceExpression(tempArrayName);
                bodyBuilder.append(new CVarDefinitionStatement(new CVariable(tempArrayName, cArrayDeclarationType)));
                final CExpression source = (outOffset == 0)? outArrayValuesRef : new CAdditionExpression(outArrayValuesRef, new CIntLiteralExpression("" + outOffset));
                bodyBuilder.append(new CAssignmentStatement(new CStructFieldAccessExpression(tempArrayRef, ARRAY_FIELD_VALUES), source));

                final ImmutableList.Builder<CExpression> paramsBuilder = new ImmutableList.Builder<CExpression>()
                        .append(new CDereferenceExpression(tempArrayRef));

                final ImmutableMap<String, PersistenceChecker.Persistence> funcPersistence = persistenceChecker.obtainExpressionPersistence(targetSpaceName.append(functionName), funcDefExp.getBody(), persistenceChecker.full);
                for (int paramIndex = 0; paramIndex < exp.getParameters().size(); paramIndex++) {
                    final String paramName = funcDefExp.getParameters().valueAt(paramIndex).getName();
                    final PersistenceChecker.Persistence paramPersistence = funcPersistence.get(paramName, null);
                    if (paramPersistence == null) {
                        paramsBuilder.append(traverseExpression(exp.getParameters().valueAt(paramIndex), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
                    }
                    else if (paramPersistence == PersistenceChecker.fullPersistence) {
                        // Assuming that the variable exists and has name outArray
                        // TODO: Adjust this logic to get the correct variable name
                        final CReferenceExpression newOutArrayRef = new CReferenceExpression("outArray");
                        assignExpressionToArray(exp.getParameters().valueAt(paramIndex), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, newOutArrayRef, true);
                        paramsBuilder.append(newOutArrayRef);
                    }
                    else if (paramPersistence instanceof PersistenceChecker.ArrayItemPersistence arrayItemParamPersistence) {
                        if (arrayItemParamPersistence.getItemPersistence() == PersistenceChecker.fullPersistence) {
                            final String arrayName = varNameCreator.create("array");
                            bodyBuilder.append(new CVarDefinitionStatement(new CVariable(arrayName, cArrayDeclarationType)));

                            final CReferenceExpression newOutArrayRef = new CReferenceExpression(arrayName);
                            assignExpressionToArray(exp.getParameters().valueAt(paramIndex), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, newOutArrayRef, false);
                            paramsBuilder.append(new CDereferenceExpression(newOutArrayRef));
                        }
                        else {
                            throw new UnsupportedOperationException("Unimplemented");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }

                final CExpression funcExp = traverseExpression(exp.getFunction(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
                ensureValidState(mTypeMap.get(exp) instanceof ArrayType);
                bodyBuilder.append(new CFunctionExecutionStatement(funcExp, paramsBuilder.build()));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof ReferenceExpression exp) {
            final CReferenceExpression valueRef = new CReferenceExpression(exp.getReference());
            final Type resultingType = mTypeMap.get(exp);
            if (resultingType instanceof ArrayType resultingArrayType) {
                bodyBuilder.append(new CFunctionExecutionStatement(new CReferenceExpression("memcpy"), new ImmutableList.Builder<CExpression>()
                        .append(outArrayValuesRef)
                        .append(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_VALUES))
                        .append(new CMultiplicationExpression(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH), new CSizeofExpression(new CPointerType(cType(resultingArrayType.getItemType(), definedStructs)))))
                        .build()));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof StringLiteralExpression exp) {
            final int arrayLength = exp.getArrayLength();
            for (int i = 0; i < arrayLength; i++) {
                final String ch = exp.getCharAt(i);
                final int chIndex = stringPool.getPool().indexOf(ch);
                ensureValidState(chIndex >= 0);
                bodyBuilder.append(new CAssignmentStatement(new CArrayValueAtExpression(outArrayValuesRef, new CIntLiteralExpression("" + (outOffset + i))), new CAdditionExpression(new CReferenceExpression(STRING_POOL), new CIntLiteralExpression("" + chIndex))));
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private void assignExpressionToArrayPointer(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer,
            String outArrayPointer) {
        if (expression instanceof IfExpression exp) {
            final CExpression condition = traverseExpression(exp.getCondition(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final ImmutableList.Builder<CInFunctionStatement> thenClauseBuilder = new ImmutableList.Builder<>();
            final ImmutableList.Builder<CInFunctionStatement> elseClauseBuilder = new ImmutableList.Builder<>();
            assignExpressionToArrayPointer(exp.getThenClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, thenClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayPointer);
            assignExpressionToArrayPointer(exp.getElseClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, elseClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayPointer);
            bodyBuilder.append(new CIfStatement(condition, thenClauseBuilder.build(), elseClauseBuilder.build()));
        }
        else if (expression instanceof ReferenceExpression exp) {
            final String ref = exp.getReference();
            final CReferenceExpression sourceRef = new CReferenceExpression(ref);
            final CExpression source = refIsPointer.contains(ref)? sourceRef : new CDereferenceExpression(sourceRef);
            bodyBuilder.append(new CAssignmentStatement(new CReferenceExpression(outArrayPointer), source));
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    /**
     * Add the corresponding statements in the current body to fill the given array.
     * <p>
     * This method assumes that the method calling here has already provided an Array structure matching the outArray name, and that its values fields has been initialized with enough room to the store the result.
     */
    private void assignExpressionToArray(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer,
            CReferenceExpression outArrayRef,
            boolean outArrayIsPointer) {
        if (expression instanceof ArrayConcatenationExpression exp) {
            final ArrayType arrayType = (ArrayType) mTypeMap.get(expression);
            final IntType lengthType = arrayType.getLengthType();
            if (lengthType.getMin().equals(lengthType.getMax())) {
                bodyBuilder.append(new CAssignmentStatement(new CStructFieldPointerAccessExpression(outArrayRef, ARRAY_FIELD_LENGTH), new CIntLiteralExpression(lengthType.getMin())));
                final CAssignableExpression outArrayValuesRef = new CStructFieldPointerAccessExpression(outArrayRef, ARRAY_FIELD_VALUES);
                assignExpressionToArrayValues(exp, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayValuesRef, 0);
            }
            else {
                assignExpressionToArray(exp.getLeftExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, outArrayIsPointer);
                final String tempArrayName = varNameCreator.create("array");
                final CReferenceExpression tempArrayRef = new CReferenceExpression(tempArrayName);
                bodyBuilder.append(new CVarDefinitionStatement(new CVariable(tempArrayName, cArrayDeclarationType)));

                final CExpression lengthAccessExpression = outArrayIsPointer? new CStructFieldPointerAccessExpression(outArrayRef, ARRAY_FIELD_LENGTH) : new CStructFieldAccessExpression(outArrayRef, ARRAY_FIELD_LENGTH);
                final CExpression valuesAccessExpression = outArrayIsPointer? new CStructFieldPointerAccessExpression(outArrayRef, ARRAY_FIELD_VALUES) : new CStructFieldAccessExpression(outArrayRef, ARRAY_FIELD_VALUES);
                bodyBuilder.append(new CAssignmentStatement(
                        new CStructFieldAccessExpression(tempArrayRef, ARRAY_FIELD_VALUES),
                        new CAdditionExpression(valuesAccessExpression, lengthAccessExpression)));
                assignExpressionToArray(exp.getRightExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, tempArrayRef, false);
                bodyBuilder.append(new CAdditionStatement(new CStructFieldPointerAccessExpression(outArrayRef, ARRAY_FIELD_LENGTH), new CStructFieldPointerAccessExpression(tempArrayRef, ARRAY_FIELD_LENGTH)));
            }
        }
        else if (expression instanceof ArrayConstructionExpression exp) {
            final ArrayType arrayType = (ArrayType) mTypeMap.get(exp);
            final ImmutableList<CExpression> params = exp.getValues().map(paramValue -> traverseExpression(paramValue, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
            final String valuesVarName = varNameCreator.create("values");
            final int arrayLength = params.size();
            bodyBuilder.append(new CArrayDefinitionStatement(new CVariable(valuesVarName, new CPointerType(cType(arrayType.getItemType(), definedStructs))), arrayLength));
            for (int i = 0; i < arrayLength; i++) {
                bodyBuilder.append(new CAssignmentStatement(new CArrayValueAtExpression(new CReferenceExpression(valuesVarName), new CIntLiteralExpression("" + i)), params.valueAt(i)));
            }

            final CAssignableExpression lengthTarget = outArrayIsPointer? newArrayLengthPointerAccessExpression(outArrayRef) : newArrayLengthAccessExpression(outArrayRef);
            bodyBuilder.append(new CAssignmentStatement(lengthTarget, new CIntLiteralExpression("" + arrayLength)));
            final CAssignableExpression valuesTarget = outArrayIsPointer? newArrayValuesPointerAccessExpression(outArrayRef) : newArrayValuesAccessExpression(outArrayRef);
            bodyBuilder.append(new CAssignmentStatement(valuesTarget, new CCastExpression(new CPointerType(new CPointerType(CVoidType.getInstance())), new CReferenceExpression(valuesVarName))));

            final String arrayName = varNameCreator.create("array");
            bodyBuilder.append(new CVarDefinitionStatement(new CVariable(arrayName, new CStructDeclarationType("Array"))));
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            final CExpression arrayExp = traverseExpression(exp.getArray(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final CExpression indexExp = traverseExpression(exp.getIndex(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final String valueVarName = varNameCreator.create("value");
            final CReferenceExpression valueRef = new CReferenceExpression(valueVarName);
            final Type resultingType = mTypeMap.get(exp);
            if (resultingType instanceof ArrayType resultingArrayType) {
                bodyBuilder.append(new CVarDefinitionStatement(new CVariable(valueVarName, new CPointerType(cType(resultingType, definedStructs)))));
                bodyBuilder.append(new CAssignmentStatement(valueRef, new CArrayValueAtExpression(arrayExp, indexExp)));
                bodyBuilder.append(new CFunctionExecutionStatement(new CReferenceExpression("memcpy"), new ImmutableList.Builder<CExpression>()
                        .append(newArrayValuesAccessExpression(outArrayRef))
                        .append(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_VALUES))
                        .append(new CMultiplicationExpression(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH), new CSizeofExpression(new CPointerType(cType(resultingArrayType.getItemType(), definedStructs)))))
                        .build()));
                bodyBuilder.append(new CAssignmentStatement(newArrayLengthAccessExpression(outArrayRef), new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH)));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof ComplexExpression exp) {
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof TypeDefinitionStatement typeDef) {
                    traverseTypeDefinitionStatement(typeDef, definedStructs, definedTypes);
                }
            }

            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    traverseConstantDefinitionStatement(constDef, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
                }
            }

            assignExpressionToArray(exp.getExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, outArrayIsPointer);
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            final CExpression funcExp = traverseExpression(exp.getFunction(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final ImmutableList<CExpression> paramsExp = exp.getParameters().map(e -> traverseExpression(e, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer));
            final String valueVarName = varNameCreator.create("value");
            final CReferenceExpression valueRef = new CReferenceExpression(valueVarName);
            final Type resultingType = mTypeMap.get(exp);
            if (resultingType instanceof ArrayType resultingArrayType) {
                bodyBuilder.append(new CVarDefinitionStatement(new CVariable(valueVarName, new CPointerType(cType(resultingType, definedStructs)))));
                bodyBuilder.append(new CFunctionExecutionStatement(funcExp, paramsExp.prepend(valueRef)));
                bodyBuilder.append(new CFunctionExecutionStatement(new CReferenceExpression("memcpy"), new ImmutableList.Builder<CExpression>()
                        .append(newArrayValuesAccessExpression(outArrayRef))
                        .append(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_VALUES))
                        .append(new CMultiplicationExpression(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH), new CSizeofExpression(new CPointerType(cType(resultingArrayType.getItemType(), definedStructs)))))
                        .build()));
                bodyBuilder.append(new CAssignmentStatement(newArrayLengthAccessExpression(outArrayRef), new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH)));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof IfExpression exp) {
            final CExpression condition = traverseExpression(exp.getCondition(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final ImmutableList.Builder<CInFunctionStatement> thenClauseBuilder = new ImmutableList.Builder<>();
            final ImmutableList.Builder<CInFunctionStatement> elseClauseBuilder = new ImmutableList.Builder<>();
            assignExpressionToArray(exp.getThenClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, thenClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, outArrayIsPointer);
            assignExpressionToArray(exp.getElseClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, elseClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, outArrayIsPointer);
            bodyBuilder.append(new CIfStatement(condition, thenClauseBuilder.build(), elseClauseBuilder.build()));
        }
        else if (expression instanceof ReferenceExpression exp) {
            final CReferenceExpression valueRef = new CReferenceExpression(exp.getReference());
            final Type resultingType = mTypeMap.get(exp);
            if (resultingType instanceof ArrayType resultingArrayType) {
                bodyBuilder.append(new CFunctionExecutionStatement(new CReferenceExpression("memcpy"), new ImmutableList.Builder<CExpression>()
                        .append(newArrayValuesAccessExpression(outArrayRef))
                        .append(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_VALUES))
                        .append(new CMultiplicationExpression(new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH), new CSizeofExpression(new CPointerType(cType(resultingArrayType.getItemType(), definedStructs)))))
                        .build()));
                bodyBuilder.append(new CAssignmentStatement(newArrayLengthAccessExpression(outArrayRef), new CStructFieldPointerAccessExpression(valueRef, ARRAY_FIELD_LENGTH)));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof StringLiteralExpression exp) {
            final CAssignableExpression target = outArrayIsPointer? newArrayLengthPointerAccessExpression(outArrayRef) :
                    newArrayLengthAccessExpression(outArrayRef);
            bodyBuilder.append(new CAssignmentStatement(target, new CIntLiteralExpression("" + exp.getArrayLength())));

            final CAssignableExpression targetValues = outArrayIsPointer? newArrayValuesPointerAccessExpression(outArrayRef) :
                    newArrayValuesAccessExpression(outArrayRef);
            assignExpressionToArrayValues(exp, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, targetValues, 0);
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private void assignExpressionToRegister(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer,
            CReferenceExpression outRegisterRef,
            boolean outRegisterIsPointer) {
        if (expression instanceof ComplexExpression exp) {
            final MutableMap<String, Expression> scopeDefinitions = MutableHashMap.empty();
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof TypeDefinitionStatement typeDef) {
                    traverseTypeDefinitionStatement(typeDef, definedStructs, definedTypes);
                }
            }

            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    traverseConstantDefinitionStatement(constDef, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, scopeDefinitions, varNameCreator, stringPool, refIsPointer);
                }
            }

            assignExpressionToRegister(exp.getExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outRegisterRef, outRegisterIsPointer);
        }
        else if (expression instanceof IfExpression exp) {
            final CExpression condition = traverseExpression(exp.getCondition(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
            final ImmutableList.Builder<CInFunctionStatement> thenClauseBuilder = new ImmutableList.Builder<>();
            final ImmutableList.Builder<CInFunctionStatement> elseClauseBuilder = new ImmutableList.Builder<>();
            assignExpressionToRegister(exp.getThenClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, thenClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outRegisterRef, outRegisterIsPointer);
            assignExpressionToRegister(exp.getElseClause(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, elseClauseBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outRegisterRef, outRegisterIsPointer);
            bodyBuilder.append(new CIfStatement(condition, thenClauseBuilder.build(), elseClauseBuilder.build()));
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            final RegisterType regType = (RegisterType) definedTypes.get(exp.getType());
            final ImmutableSet<String> fieldNames = regType.getFields().keySet();
            for (Statement statement : exp.getStatements()) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    final String name = constDef.getName();
                    if (fieldNames.contains(name)) {
                        bodyBuilder.append(new CAssignmentStatement(new CStructFieldPointerAccessExpression(outRegisterRef, name), traverseExpression(constDef.getExpression(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer)));
                    }
                    else {
                        traverseConstantDefinitionStatement(constDef, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            if (exp.getFunction() instanceof ReferenceExpression refExp) {
                final String functionName = refExp.getReference();
                ImmutableList<String> targetSpaceName = spaceName;
                FunctionDefinitionExpression tempFuncDefExp = null;
                do {
                    tempFuncDefExp = functionMap.get(targetSpaceName.append(functionName), null);
                    if (tempFuncDefExp == null) {
                        ensureValidState(!targetSpaceName.isEmpty());
                        targetSpaceName = targetSpaceName.skipLast(1);
                    }
                }
                while (tempFuncDefExp == null);

                final FunctionDefinitionExpression funcDefExp = tempFuncDefExp;
                ensureValidState(exp.getParameters().size() == funcDefExp.getParameters().size());

                final ImmutableMap<String, PersistenceChecker.Persistence> funcPersistence = persistenceChecker.obtainExpressionPersistence(targetSpaceName.append(functionName), funcDefExp.getBody(), persistenceChecker.full);
                final ImmutableList.Builder<CExpression> paramsBuilder = new ImmutableList.Builder<CExpression>()
                        .append(outRegisterRef);

                for (int paramIndex = 0; paramIndex < exp.getParameters().size(); paramIndex++) {
                    final String paramName = funcDefExp.getParameters().valueAt(paramIndex).getName();
                    final PersistenceChecker.Persistence paramPersistence = funcPersistence.get(paramName, null);
                    if (paramPersistence == PersistenceChecker.fullPersistence) {
                        // Assuming that the variable exists and has name outArray0
                        // TODO: Adjust this logic to get the correct variable name
                        final CReferenceExpression outArrayRef = new CReferenceExpression("outArray0");
                        assignExpressionToArray(exp.getParameters().valueAt(paramIndex), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, true);
                        paramsBuilder.append(outArrayRef);
                    }
                    else if (paramPersistence instanceof PersistenceChecker.ArrayItemPersistence arrayItemParamPersistence) {
                        if (arrayItemParamPersistence.getItemPersistence() == PersistenceChecker.fullPersistence) {
                            final String arrayName = varNameCreator.create("array");
                            bodyBuilder.append(new CVarDefinitionStatement(new CVariable(arrayName, cArrayDeclarationType)));

                            final CReferenceExpression outArrayRef = new CReferenceExpression(arrayName);
                            assignExpressionToArray(exp.getParameters().valueAt(paramIndex), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, outArrayRef, false);
                            paramsBuilder.append(new CDereferenceExpression(outArrayRef));
                        }
                        else {
                            throw new UnsupportedOperationException("Unimplemented");
                        }
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                }

                final CExpression funcExp = traverseExpression(exp.getFunction(), spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
                ensureValidState(mTypeMap.get(exp) instanceof RegisterType);
                bodyBuilder.append(new CFunctionExecutionStatement(funcExp, paramsBuilder.build()));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    /**
     * Whether the result of this expression can be just a pointer in C.
     * <p>
     * Usually it is OK to be a pointer if the expression does not build
     * any new data structure, and an existing one can be reused instead.
     */
    private boolean isPointerEnough(Expression exp) {
        if (exp instanceof ReferenceExpression) {
            return true;
        }
        else if (exp instanceof IfExpression e) {
            return isPointerEnough(e.getThenClause()) && isPointerEnough(e.getElseClause());
        }
        else {
            return false;
        }
    }

    private static final class RegisterStructTypes {
        private final CStructDeclarationType mDeclaration;
        private final CStructType mType;

        RegisterStructTypes(CStructDeclarationType declaration, CStructType type) {
            mDeclaration = declaration;
            mType = type;
        }

        public CStructDeclarationType getDeclaration() {
            return mDeclaration;
        }

        public CStructType getType() {
            return mType;
        }
    }

    private void traverseTypeDefinitionStatement(
            TypeDefinitionStatement statement,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            MutableMap<String, Type> definedTypes) {
        final String typeName = statement.getName();
        final Type type = statement.getType();
        if (type instanceof RegisterType regType) {
            final ImmutableMap<String, Type> regFields = regType.getFields();
            final CStructType newType = new CStructType(typeName, regFields.keySet().map(fieldName -> {
                final Type fieldType = regFields.get(fieldName);
                final CTypeDeclaration fieldRawCType = cType(fieldType, definedStructs);
                final CTypeDeclaration fieldCType = (fieldType instanceof ArrayType || fieldType instanceof RegisterType)?
                        new CPointerType(fieldRawCType) : fieldRawCType;
                return new CVariable(fieldName, fieldCType);
            }));
            definedStructs.put(((RegisterType) type).getDefinition(), new RegisterStructTypes(new CStructDeclarationType(typeName), newType));
        }
        definedTypes.put(typeName, type);
    }

    private void findFunctionDefinitionsInStatement(
            Statement statement,
            ImmutableList<String> spaceName,
            MutableMap<ImmutableList<String>, FunctionDefinitionExpression> outFunctions) {
        if (statement instanceof ConstantDefinitionStatement constDef) {
            final ImmutableList<String> innerSpaceName = spaceName.append(statement.getName());
            findFunctionDefinitionsInExpression(constDef.getExpression(), innerSpaceName, outFunctions);
        }
    }

    private void findFunctionDefinitionsInExpression(
            Expression expression,
            ImmutableList<String> spaceName,
            MutableMap<ImmutableList<String>, FunctionDefinitionExpression> outFunctions) {
        if (expression instanceof ArrayConstructionExpression exp) {
            for (Expression parameter : exp.getValues()) {
                findFunctionDefinitionsInExpression(parameter, spaceName, outFunctions);
            }
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            findFunctionDefinitionsInExpression(exp.getArray(), spaceName, outFunctions);
            findFunctionDefinitionsInExpression(exp.getIndex(), spaceName, outFunctions);
        }
        else if (expression instanceof ComplexExpression exp) {
            for (Statement statement : exp.getStatements()) {
                findFunctionDefinitionsInStatement(statement, spaceName, outFunctions);
            }

            findFunctionDefinitionsInExpression(exp.getExpression(), spaceName, outFunctions);
        }
        else if (expression instanceof FunctionDefinitionExpression exp) {
            outFunctions.put(spaceName, exp);
            findFunctionDefinitionsInExpression(exp.getBody(), spaceName, outFunctions);
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            for (Expression parameter : exp.getParameters()) {
                findFunctionDefinitionsInExpression(parameter, spaceName, outFunctions);
            }

            findFunctionDefinitionsInExpression(exp.getFunction(), spaceName, outFunctions);
        }
        else if (expression instanceof IfExpression exp) {
            findFunctionDefinitionsInExpression(exp.getCondition(), spaceName, outFunctions);
            findFunctionDefinitionsInExpression(exp.getThenClause(), spaceName, outFunctions);
            findFunctionDefinitionsInExpression(exp.getElseClause(), spaceName, outFunctions);
        }
        else if (expression instanceof LeftRightExpression exp) {
            findFunctionDefinitionsInExpression(exp.getLeftExpression(), spaceName, outFunctions);
            findFunctionDefinitionsInExpression(exp.getRightExpression(), spaceName, outFunctions);
        }
        else if (expression instanceof LiteralExpression || expression instanceof ReferenceExpression) {
            // Nothing to be done
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            for (Statement statement : exp.getStatements()) {
                findFunctionDefinitionsInStatement(statement, spaceName, outFunctions);
            }
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            findFunctionDefinitionsInExpression(exp.getRegister(), spaceName, outFunctions);
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private void traverseConstantDefinitionStatement(
            ConstantDefinitionStatement statement,
            ImmutableList<String> spaceName,
            MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs,
            ImmutableMap<ImmutableList<String>, FunctionDefinitionExpression> functionMap,
            PersistenceChecker persistenceChecker,
            RequiredAllocationsChecker requiredAllocationsChecker,
            ImmutableList.Builder<CFunction> cFunctionsBuilder,
            ImmutableList.Builder<CInFunctionStatement> bodyBuilder,
            MutableMap<String, Type> definedTypes,
            MutableMap<String, Expression> definedConstants,
            VariableNameCreator varNameCreator,
            StringPoolGenerator.Result stringPool,
            Set<String> refIsPointer) {
        final Expression constDefExp = statement.getExpression();
        if (constDefExp instanceof FunctionDefinitionExpression funcExpression) {
            final boolean isPublic = spaceName.isEmpty();
            final ImmutableList<String> innerSpaceName = spaceName.append(statement.getName());

            final RequiredAllocationsChecker.Result funcRequiredAllocations = requiredAllocationsChecker.obtainCheckedExpression(innerSpaceName, funcExpression.getBody(), persistenceChecker.full);
            final MutableMap<String, Type> paramTypes = MutableHashMap.empty();
            final MutableSet<String> newRefIsPointer = MutableHashSet.empty();
            for (FunctionParameter funcParam : funcExpression.getParameters()) {
                final Type paramType = funcParam.getType();
                paramTypes.put(funcParam.getName(), paramType);
                if (paramType instanceof RegisterType || paramType instanceof ArrayType) {
                    newRefIsPointer.add(funcParam.getName());
                }
            }

            final Type resultType = mTypeMap.get(funcExpression.getBody());
            final ImmutableList.Builder<CVariable> paramsBuilder = new ImmutableList.Builder<>();
            boolean resultAsParameter = false;
            if (resultType instanceof ArrayType && !funcRequiredAllocations.getPointersInArray().isEmpty()) {
                paramsBuilder.append(new CVariable(OUT_RESULT, new CPointerType(cArrayDeclarationType)));
                newRefIsPointer.add(OUT_RESULT);
                resultAsParameter = true;
            }
            else if (resultType instanceof RegisterType regType && funcRequiredAllocations.getStructs().containsKey(regType.getDefinition())) {
                paramsBuilder.append(new CVariable(OUT_RESULT, new CPointerType(cType(resultType, definedStructs))));
                newRefIsPointer.add(OUT_RESULT);
                resultAsParameter = true;
            }

            for (IntValueMap.Entry<RegisterType.Definition> entry : funcRequiredAllocations.getStructs().entries()) {
                for (int i = 0; i < entry.value(); i++) {
                    if (resultType instanceof RegisterType regType && regType.getDefinition() == entry.key() && i == 0) {
                        // Result as parameter already added
                    }
                    else {
                        final String varName = "outRegister" + i;
                        paramsBuilder.append(new CVariable(varName, new CPointerType(definedStructs.get(entry.key()).getDeclaration())));
                        newRefIsPointer.add(varName);
                    }
                }
            }

            for (int index : funcRequiredAllocations.getPointersInArray().indexes()) {
                if (resultType instanceof ArrayType && index == 0) {
                    // Result as parameter already added
                }
                else {
                    final String varName = "outArray" + index;
                    paramsBuilder.append(new CVariable(varName, new CPointerType(cArrayDeclarationType)));
                    newRefIsPointer.add(varName);
                }
            }

            for (String dependency : funcExpression.dependencies()) {
                final Expression dependencyExp = definedConstants.get(dependency);
                if (!(dependencyExp instanceof StringLiteralExpression || dependencyExp instanceof ArrayConstructionExpression depArrayCons && depArrayCons.getValues().allMatch(v -> v instanceof StringLiteralExpression))) {
                    final Type dependencyType = mTypeMap.get(dependencyExp);
                    final CTypeDeclaration cParamType;
                    if (dependencyType instanceof ArrayType arrayType) {
                        final String lengthMinText = arrayType.getLengthType().getMin();
                        final String lengthMaxText = arrayType.getLengthType().getMax();
                        cParamType = new CPointerType(lengthMinText.equals(lengthMaxText) ?
                                cType(arrayType.getItemType(), definedStructs) :
                                cType(dependencyType, definedStructs));
                    }
                    else {
                        cParamType = cType(dependencyType, definedStructs);
                    }

                    paramsBuilder.append(new CVariable(dependency, cParamType));
                    if (dependencyType instanceof ArrayType || dependencyType instanceof RegisterType) {
                        newRefIsPointer.add(dependency);
                    }
                }
            }

            final ImmutableList.Builder<CInFunctionStatement> innerBodyBuilder = new ImmutableList.Builder<>();
            if (resultType instanceof ArrayType arrayType && !funcRequiredAllocations.getPointersInArray().isEmpty()) {
                if (arrayType.getLengthType().getMin().equals(arrayType.getLengthType().getMax())) {
                    assignExpressionToArray(funcExpression.getBody(), innerSpaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, innerBodyBuilder, definedTypes, definedConstants, new DefaultVariableNameCreator(), stringPool, newRefIsPointer, OUT_RESULT_REF, false);
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented for non-fixed length array");
                }
            }
            else if (resultType instanceof RegisterType regType && funcRequiredAllocations.getStructs().containsKey(regType.getDefinition())) {
                assignExpressionToRegister(funcExpression.getBody(), innerSpaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, innerBodyBuilder, definedTypes, definedConstants, new DefaultVariableNameCreator(), stringPool, newRefIsPointer, OUT_RESULT_REF, true);
            }
            else {
                traverseExpression(funcExpression.getBody(), innerSpaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, innerBodyBuilder, definedTypes, definedConstants, new DefaultVariableNameCreator(), stringPool, newRefIsPointer);
            }

            for (FunctionParameter param : funcExpression.getParameters()) {
                final Type paramType = param.getType();
                final CTypeDeclaration cType = cType(paramType, definedStructs);
                final CTypeDeclaration ptrType = (paramType instanceof ArrayType || paramType instanceof RegisterType)? new CPointerType(cType) : cType;
                paramsBuilder.append(new CVariable(param.getName(), ptrType));
            }

            final String functionName = innerSpaceName.reduce((a, b) -> a + "_" + b);
            final CTypeDeclaration resultCType = resultAsParameter? CVoidType.getInstance() : cType(resultType, definedStructs);
            cFunctionsBuilder.append(new CFunction(functionName, paramsBuilder.build(), resultCType, innerBodyBuilder.build(), !isPublic));
        }
        else if (constDefExp instanceof IfExpression ifExpression) {
            final Type ifResultingType = mTypeMap.get(ifExpression);
            if (ifResultingType instanceof ArrayType arrayResultingType) {
                final String ifResultVarName = varNameCreator.create(statement.getName());
                final CTypeDeclaration targetType = cType(arrayResultingType, definedStructs);
                if (isPointerEnough(ifExpression)) {
                    final MutableSet<String> newRefIsPointer = refIsPointer.mutate();
                    newRefIsPointer.add(ifResultVarName);
                    bodyBuilder.append(new CVarDefinitionStatement(new CVariable(ifResultVarName, new CPointerType(targetType))));
                    assignExpressionToArrayPointer(ifExpression, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, newRefIsPointer, ifResultVarName);
                }
                else {
                    final String maxLengthText = arrayResultingType.getLengthType().getMax();
                    if (maxLengthText.equals(TypeConstants.unboundText)) {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                    else {
                        final CReferenceExpression ifResultRef = new CReferenceExpression(ifResultVarName);
                        final String valuesVarName = varNameCreator.create("values");
                        final int arrayLength = Integer.parseInt(maxLengthText);
                        final Type itemType = arrayResultingType.getItemType();
                        final CTypeDeclaration cItemType = cType(itemType, definedStructs);
                        final CTypeDeclaration cItemPtrType = new CPointerType(cItemType);
                        bodyBuilder.append(new CArrayDefinitionStatement(new CVariable(valuesVarName, cItemPtrType), arrayLength));
                        bodyBuilder.append(new CVarDefinitionStatement(new CVariable(ifResultVarName, targetType)));
                        bodyBuilder.append(new CAssignmentStatement(newArrayValuesAccessExpression(ifResultRef), new CCastExpression(new CPointerType(new CPointerType(CVoidType.getInstance())), new CReferenceExpression(valuesVarName))));
                        assignExpressionToArray(ifExpression, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, ifResultRef, false);
                    }
                }
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else {
            final String newName = varNameCreator.create(statement.getName());
            final Type constType = mTypeMap.get(constDefExp);
            bodyBuilder.append(new CVarDefinitionStatement(new CVariable(newName, cType(constType, definedStructs))));

            final CReferenceExpression varRef = new CReferenceExpression(statement.getName());
            if (constType instanceof RegisterType) {
                assignExpressionToRegister(constDefExp, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, varRef, false);
            }
            else if (constType instanceof ArrayType) {
                assignExpressionToArray(constDefExp, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer, varRef, false);
            }
            else {
                final CExpression resultExpression = traverseExpression(constDefExp, spaceName, definedStructs, functionMap, persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, bodyBuilder, definedTypes, definedConstants, varNameCreator, stringPool, refIsPointer);
                bodyBuilder.append(new CAssignmentStatement(varRef, resultExpression));
            }
        }

        definedConstants.put(statement.getName(), constDefExp);
    }

    private static final class ScopedName {
        private final ScopedName parent;
        private final String name;

        ScopedName(ScopedName parent, String name) {
            ensureNonNull(name);
            this.parent = parent;
            this.name = name;
        }

        ScopedName getParent() {
            return parent;
        }

        String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return (name != null)? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof ScopedName that &&
                    name.equals(that.name) &&
                    Objects.equals(parent, that.parent);
        }

        public String toCNameSpace() {
            return (parent == null)? name : parent.toCNameSpace() + "_" + name;
        }
    }

    private static final class ConstantDependencies {
        final ScopedName name;
        final ConstantDefinitionStatement definitionStatement;

        ConstantDependencies(ScopedName name, ConstantDefinitionStatement definitionStatement) {
            ensureNonNull(name, definitionStatement);
            this.name = name;
            this.definitionStatement = definitionStatement;
        }

        public ScopedName getName() {
            return name;
        }

        public ConstantDefinitionStatement getDefinitionStatement() {
            return definitionStatement;
        }
    }

    private static final class IntegerHolder {
        int value;
    }

    public Result generate(ImmutableList<Statement> statements) {
        final StringPoolGenerator stringPoolGenerator = new StringPoolGenerator();
        final StringPoolGenerator.Result stringPoolGeneratorResult = stringPoolGenerator.generate(statements);

        final MutableMap<ImmutableList<String>, FunctionDefinitionExpression> foundFunctions = MutableHashMap.empty();
        for (Statement statement : statements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                findFunctionDefinitionsInStatement(constDef, ImmutableList.empty(), foundFunctions);
            }
        }

        final PersistenceChecker persistenceChecker = new PersistenceChecker(mTypeMap, foundFunctions.toImmutable());
        final RequiredAllocationsChecker requiredAllocationsChecker = new RequiredAllocationsChecker(mTypeMap, foundFunctions.toImmutable(), persistenceChecker);

        final MutableMap<RegisterType.Definition, RegisterStructTypes> definedStructs = MutableHashMap.empty();
        final ImmutableList.Builder<CFunction> cFunctionsBuilder = new ImmutableList.Builder<>();
        final MutableMap<String, Type> definedTypes = MutableHashMap.empty();

        for (Statement statement : statements) {
            if (statement instanceof TypeDefinitionStatement typeDef) {
                traverseTypeDefinitionStatement(typeDef, definedStructs, definedTypes);
            }
        }

        final ImmutableHashSet.Builder<String> refIsPointerBuilder = new ImmutableHashSet.Builder<>();
        for (Statement statement : statements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                final Type statementType = mTypeMap.get(constDef.getExpression());
                if (statementType instanceof ArrayType || statementType instanceof RegisterType) {
                    refIsPointerBuilder.add(constDef.getName());
                }
            }
        }

        final ImmutableSet<String> refIsPointer = refIsPointerBuilder.build();
        for (Statement statement : statements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                traverseConstantDefinitionStatement(constDef, ImmutableList.empty(), definedStructs, foundFunctions.toImmutable(), persistenceChecker, requiredAllocationsChecker, cFunctionsBuilder, new ImmutableList.Builder<>(), definedTypes, MutableHashMap.empty(), new DefaultVariableNameCreator(), stringPoolGeneratorResult, refIsPointer);
            }
        }

        final ImmutableList<CStructType> cStructs = definedStructs.toList().map(RegisterStructTypes::getType).toImmutable();
        final ImmutableList<CFunction> cFunctions = cFunctionsBuilder.build();

        final ImmutableList.Builder<CFileRootStatement> headerStatementsBuilder = new ImmutableList.Builder<>();
        headerStatementsBuilder.append(new CStructDefinitionStatement(new CStructType("Array", new ImmutableList.Builder<CVariable>()
                .append(new CVariable(ARRAY_FIELD_LENGTH, CIntType.getInstance()))
                .append(new CVariable(ARRAY_FIELD_VALUES, new CPointerType(new CPointerType(CVoidType.getInstance()))))
                .build())));
        for (CStructType cStruct : cStructs) {
            headerStatementsBuilder.append(new CStructDefinitionStatement(cStruct));
        }

        for (CFunction cFunction : cFunctions) {
            if (!cFunction.isStatic()) {
                headerStatementsBuilder.append(new CFunctionDeclarationStatement(cFunction));
            }
        }

        final ImmutableList.Builder<CFileRootStatement> sourceStatementsBuilder = new ImmutableList.Builder<>();
        final String stringPool = stringPoolGeneratorResult.getPool();
        if (!stringPool.isEmpty()) {
            sourceStatementsBuilder.append(new CConstantDefinitionStatement(STRING_POOL, new CPointerType(CCharType.getInstance()), new CStringLiteralExpression(stringPool)));
        }

        for (CFunction cFunction : cFunctions) {
            sourceStatementsBuilder.append(new CFunctionDefinitionStatement(cFunction));
        }

        return new Result(headerStatementsBuilder.build(), sourceStatementsBuilder.build());
    }

    public static final class Result {
        private final ImmutableList<CFileRootStatement> mHeaderStatements;
        private final ImmutableList<CFileRootStatement> mSourceStatements;

        Result(ImmutableList<CFileRootStatement> headerStatements, ImmutableList<CFileRootStatement> sourceStatements) {
            ensureNonNull(headerStatements, sourceStatements);
            mHeaderStatements = headerStatements;
            mSourceStatements = sourceStatements;
        }

        public ImmutableList<CFileRootStatement> getHeaderStatements() {
            return mHeaderStatements;
        }

        public ImmutableList<CFileRootStatement> getSourceStatements() {
            return mSourceStatements;
        }
    }
}
