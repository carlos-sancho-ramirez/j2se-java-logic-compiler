package sword.logic.compiler.generator.java;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableHashSet;
import sword.collections.MutableList;
import sword.collections.MutableMap;
import sword.collections.MutableSet;
import sword.collections.Set;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.generator.java.entities.JavaClass;
import sword.logic.compiler.generator.java.entities.JavaClassReference;
import sword.logic.compiler.generator.java.entities.JavaEnum;
import sword.logic.compiler.generator.java.entities.JavaField;
import sword.logic.compiler.generator.java.entities.JavaMethodReference;
import sword.logic.compiler.generator.java.entities.JavaNormalClass;
import sword.logic.compiler.generator.java.entities.JavaPackage;
import sword.logic.compiler.generator.java.entities.JavaVariable;
import sword.logic.compiler.generator.java.expressions.JavaAdditionExpression;
import sword.logic.compiler.generator.java.expressions.JavaAndExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayConcatenationExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayLengthExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayValueAtExpression;
import sword.logic.compiler.generator.java.expressions.JavaDivisionExpression;
import sword.logic.compiler.generator.java.expressions.JavaEnumValueLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaExpression;
import sword.logic.compiler.generator.java.expressions.JavaFieldAccessExpression;
import sword.logic.compiler.generator.java.expressions.JavaFunctionExecutionExpression;
import sword.logic.compiler.generator.java.expressions.JavaGreaterOrEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaIntegerLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaLowerThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaModuleExpression;
import sword.logic.compiler.generator.java.expressions.JavaMultiplicationExpression;
import sword.logic.compiler.generator.java.expressions.JavaNewExpression;
import sword.logic.compiler.generator.java.expressions.JavaOrExpression;
import sword.logic.compiler.generator.java.expressions.JavaReferenceExpression;
import sword.logic.compiler.generator.java.expressions.JavaStringLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaTernaryExpression;
import sword.logic.compiler.generator.java.statements.JavaAssignmentStatement;
import sword.logic.compiler.generator.java.statements.JavaIfStatement;
import sword.logic.compiler.generator.java.statements.JavaReturnStatement;
import sword.logic.compiler.generator.java.statements.JavaStatement;
import sword.logic.compiler.generator.java.types.JavaArrayType;
import sword.logic.compiler.generator.java.types.JavaBooleanType;
import sword.logic.compiler.generator.java.types.JavaByteType;
import sword.logic.compiler.generator.java.types.JavaIntType;
import sword.logic.compiler.generator.java.types.JavaShortType;
import sword.logic.compiler.generator.java.types.JavaType;
import sword.logic.expressions.AdditionExpression;
import sword.logic.expressions.AndExpression;
import sword.logic.expressions.ArrayConcatenationExpression;
import sword.logic.expressions.ArrayConstructionExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
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

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

public final class JavaCodeGenerator {
    private final JavaClassReference mOutputClassRef;
    private final ImmutableMap<Expression, Type> mTypeMap;
    private final JavaCodeStyleForRegister mCodeStyleForRegister = new JavaBeansCodeStyleForRegister();

    public JavaCodeGenerator(
            JavaClassReference outputClassReference,
            ImmutableMap<Expression, Type> expressionTypeMap) {
        mOutputClassRef = outputClassReference;
        mTypeMap = expressionTypeMap;
    }

    static boolean arrayShouldBeString(ArrayType arrayType) {
        return arrayType.getItemType() instanceof IntType itemType &&
                IntegerLiteralOperations.greaterOrEqualThan(itemType.getMin(), "0") &&
                IntegerLiteralOperations.lowerOrEqualThan(itemType.getMax(), "127");
    }

    private JavaClassReference javaLangStringClassRef;

    private ImmutableSet<TypeDefinitionStatement> findAllTypeDefinitions(Expression expression) {
        if (expression instanceof ArrayConstructionExpression exp) {
            ImmutableSet<TypeDefinitionStatement> result = ImmutableHashSet.empty();
            for (Expression value : exp.getValues()) {
                result = result.addAll(findAllTypeDefinitions(value));
            }

            return result;
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            return findAllTypeDefinitions(exp.getArray())
                    .addAll(findAllTypeDefinitions(exp.getIndex()));
        }
        else if (expression instanceof ComplexExpression exp) {
            return findAllTypeDefinitions(exp.getStatements()).addAll(findAllTypeDefinitions(exp.getExpression()));
        }
        else if (expression instanceof FunctionDefinitionExpression exp) {
            return findAllTypeDefinitions(exp.getBody());
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            ImmutableSet<TypeDefinitionStatement> result = findAllTypeDefinitions(exp.getFunction());
            for (Expression value : exp.getParameters()) {
                result = result.addAll(findAllTypeDefinitions(value));
            }

            return result;
        }
        else if (expression instanceof IfExpression exp) {
            return findAllTypeDefinitions(exp.getCondition())
                    .addAll(findAllTypeDefinitions(exp.getThenClause()))
                    .addAll(findAllTypeDefinitions(exp.getElseClause()));
        }
        else if (expression instanceof LeftRightExpression exp) {
            return findAllTypeDefinitions(exp.getLeftExpression())
                    .addAll(findAllTypeDefinitions(exp.getRightExpression()));
        }
        else if (expression instanceof LiteralExpression) {
            return ImmutableHashSet.empty();
        }
        else if (expression instanceof ReferenceExpression) {
            return ImmutableHashSet.empty();
        }
        else if (expression instanceof RegisterConstructionExpression exp) {
            return findAllTypeDefinitions(exp.getStatements());
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            return findAllTypeDefinitions(exp.getRegister());
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private ImmutableSet<TypeDefinitionStatement> findAllTypeDefinitions(ImmutableList<Statement> statements) {
        ImmutableSet<TypeDefinitionStatement> result = ImmutableHashSet.empty();
        for (Statement statement : statements) {
            if (statement instanceof TypeDefinitionStatement typeDef) {
                result = result.add(typeDef);
            }
            else {
                final ConstantDefinitionStatement constDef = (ConstantDefinitionStatement) statement;
                result = result.addAll(findAllTypeDefinitions(constDef.getExpression()));
            }
        }

        return result;
    }

    private ImmutableMap<RegisterType.Definition, JavaClassReference> buildRegistersMap(
            JavaPackage javaPackage,
            ImmutableSet<TypeDefinitionStatement> allTypeDefinitions,
            Set<JavaClassReference> inUse) {
        final MutableHashMap<RegisterType.Definition, JavaClassReference> resultMap = MutableHashMap.empty();
        for (TypeDefinitionStatement typeDef : allTypeDefinitions) {
            if (typeDef.getType() instanceof RegisterType regType) {
                final RegisterType.Definition regDef = regType.getDefinition();
                final String desiredName = typeDef.getName();
                if (resultMap.anyMatch(ref -> ref.getName().equals(desiredName)) || inUse.anyMatch(ref -> ref.getName().equals(desiredName))) {
                    final int retrials = 100;
                    int i;
                    for (i = 2; i < retrials; i++) {
                        final String newProposedName = desiredName + i;
                        if (resultMap.allMatch(ref -> !ref.getName().equals(newProposedName)) && inUse.allMatch(ref -> !ref.getName().equals(desiredName))) {
                            resultMap.put(regDef, javaPackage.newClassReference(desiredName));
                            break;
                        }
                    }

                    if (i == retrials) {
                        throw new RuntimeException("Unable to find a suitable class name for register");
                    }
                }
                else {
                    resultMap.put(regDef, javaPackage.newClassReference(desiredName));
                }
            }
        }

        return resultMap.toImmutable();
    }

    private ImmutableMap<EnumType.Definition, JavaClassReference> buildEnumsMap(
            JavaPackage javaPackage,
            ImmutableSet<TypeDefinitionStatement> allTypeDefinitions,
            Set<JavaClassReference> inUse) {
        final MutableHashMap<EnumType.Definition, JavaClassReference> resultMap = MutableHashMap.empty();
        for (TypeDefinitionStatement typeDef : allTypeDefinitions) {
            if (typeDef.getType() instanceof EnumType enumType) {
                final EnumType.Definition enumDef = enumType.getDefinition();
                final String desiredName = typeDef.getName();
                if (resultMap.anyMatch(ref -> ref.getName().equals(desiredName)) || inUse.anyMatch(ref -> ref.getName().equals(desiredName))) {
                    final int retrials = 100;
                    int i;
                    for (i = 2; i < retrials; i++) {
                        final String newProposedName = desiredName + i;
                        if (resultMap.allMatch(ref -> !ref.getName().equals(newProposedName)) && inUse.allMatch(ref -> !ref.getName().equals(newProposedName))) {
                            resultMap.put(enumDef, javaPackage.newClassReference(desiredName));
                            break;
                        }
                    }

                    if (i == retrials) {
                        throw new RuntimeException("Unable to find a suitable class name for register");
                    }
                }
                else {
                    resultMap.put(enumDef, javaPackage.newClassReference(desiredName));
                }
            }
        }

        return resultMap.toImmutable();
    }

    private final class MethodFinder {
        private final ImmutableMap<TypeDefinitionStatement, JavaEnum> mDeclaredEnums;

        MethodFinder(ImmutableMap<TypeDefinitionStatement, JavaEnum> declaredEnums) {
            ensureNonNull(declaredEnums);
            mDeclaredEnums = declaredEnums;
        }

        private ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> findAllMethods(Expression expression, ImmutableSet<JavaEnum> enumsInScope) {
            if (expression instanceof ArrayConstructionExpression exp) {
                ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> result = ImmutableHashMap.empty();
                for (Expression value : exp.getValues()) {
                    result = result.putAll(findAllMethods(value, enumsInScope));
                }

                return result;
            }
            else if (expression instanceof ArrayValueAtExpression exp) {
                return findAllMethods(exp.getArray(), enumsInScope)
                        .putAll(findAllMethods(exp.getIndex(), enumsInScope));
            }
            else if (expression instanceof ComplexExpression exp) {
                final ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> statementsResult = findAllMethods(exp.getStatements(), enumsInScope);

                for (Statement statement : exp.getStatements()) {
                    final JavaEnum statementEnum = (statement instanceof TypeDefinitionStatement typeDef)? mDeclaredEnums.get(typeDef, null) : null;
                    if (statementEnum != null) {
                        enumsInScope = enumsInScope.add(statementEnum);
                    }
                }

                return statementsResult.putAll(findAllMethods(exp.getExpression(), enumsInScope));
            }
            else if (expression instanceof FunctionDefinitionExpression exp) {
                return findAllMethods(exp.getBody(), enumsInScope);
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> result = findAllMethods(exp.getFunction(), enumsInScope);
                for (Expression value : exp.getParameters()) {
                    result = result.putAll(findAllMethods(value, enumsInScope));
                }

                return result;
            }
            else if (expression instanceof IfExpression exp) {
                return findAllMethods(exp.getCondition(), enumsInScope)
                        .putAll(findAllMethods(exp.getThenClause(), enumsInScope))
                        .putAll(findAllMethods(exp.getElseClause(), enumsInScope));
            }
            else if (expression instanceof LeftRightExpression exp) {
                return findAllMethods(exp.getLeftExpression(), enumsInScope)
                        .putAll(findAllMethods(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof LiteralExpression) {
                return ImmutableHashMap.empty();
            }
            else if (expression instanceof ReferenceExpression) {
                return ImmutableHashMap.empty();
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                return findAllMethods(exp.getStatements(), enumsInScope);
            }
            else if (expression instanceof RegisterFieldAccessExpression exp) {
                return findAllMethods(exp.getRegister(), enumsInScope);
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }

        private ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> findAllMethods(ImmutableList<Statement> statements, ImmutableSet<JavaEnum> enumsInScope) {
            ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> result = ImmutableHashMap.empty();

            for (Statement statement : statements) {
                final JavaEnum statementEnum = (statement instanceof TypeDefinitionStatement typeDef)? mDeclaredEnums.get(typeDef, null) : null;
                if (statementEnum != null) {
                    enumsInScope = enumsInScope.add(statementEnum);
                }
            }

            for (Statement statement : statements) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    if (constDef.getExpression() instanceof FunctionDefinitionExpression) {
                        result = result.put(constDef, enumsInScope);
                    }

                    result = result.putAll(findAllMethods(constDef.getExpression(), enumsInScope));
                }
            }

            return result;
        }
    }

    private ImmutableMap<FunctionDefinitionExpression, JavaMethodReference> buildMethodsMap(
            JavaNormalClass javaClass,
            ImmutableSet<ConstantDefinitionStatement> statements,
            Map<EnumType.Definition, JavaClassReference> definedEnums,
            Map<RegisterType.Definition, JavaClassReference> definedRegisters) {
        final MutableHashMap<FunctionDefinitionExpression, JavaMethodReference> resultMap = MutableHashMap.empty();
        for (ConstantDefinitionStatement statement : statements) {
            final FunctionDefinitionExpression funcDef = (FunctionDefinitionExpression) statement.getExpression();
            final ImmutableList<JavaType> paramTypes = funcDef.getParameters().map(p -> transformToJavaType(p.getType(), definedEnums, definedRegisters));
            final JavaType returnType = transformToJavaType(mTypeMap.get(funcDef.getBody()), definedEnums, definedRegisters);
            final JavaMethodReference desiredReference = javaClass.newMethodReference(statement.getName(), paramTypes, returnType);
            if (resultMap.anyMatch(ref -> ref.equals(desiredReference))) {
                final int retrials = 100;
                int i;
                for (i = 2; i < retrials; i++) {
                    final JavaMethodReference newProposedRef = javaClass.newMethodReference(statement.getName() + i, paramTypes, returnType);
                    if (resultMap.allMatch(ref -> !ref.equals(newProposedRef))) {
                        resultMap.put(funcDef, newProposedRef);
                        break;
                    }
                }

                if (i == retrials) {
                    throw new RuntimeException("Unable to find a suitable class name for register");
                }
            }
            else {
                resultMap.put(funcDef, desiredReference);
            }
        }

        return resultMap.toImmutable();
    }

    private JavaType transformToJavaType(
            Type type,
            Map<EnumType.Definition, JavaClassReference> definedEnums,
            Map<RegisterType.Definition, JavaClassReference> definedRegisters) {
        if (type instanceof EnumType enumType) {
            if (enumType.isBooleanType()) {
                return JavaBooleanType.getInstance();
            }
            else {
                return definedEnums.get(enumType.getDefinition());
            }
        }
        else if (type instanceof ArrayType arrayType) {
            if (arrayShouldBeString(arrayType)) {
                if (javaLangStringClassRef == null) {
                    javaLangStringClassRef = JavaPackage.createPackage("java")
                            .newSubpackage("lang")
                            .newClassReference("String");
                }

                return javaLangStringClassRef;
            }
            else {
                return new JavaArrayType(transformToJavaType(arrayType.getItemType(), definedEnums, definedRegisters));
            }
        }
        else if (type instanceof IntType intType) {
            final String min = intType.getMin();
            final String max = intType.getMax();
            if (min.equals(TypeConstants.unboundText) || max.equals(TypeConstants.unboundText)) {
                throw new UnsupportedOperationException("Unimplemented");
            }
            else {
                if (IntegerLiteralOperations.lowerThan(IntegerLiteralOperations.subtraction(max, min), "256")) {
                    return JavaByteType.getInstance();
                }
                else if (IntegerLiteralOperations.lowerThan(IntegerLiteralOperations.subtraction(max, min), "65536")) {
                    return JavaShortType.getInstance();
                }
                else {
                    return JavaIntType.getInstance();
                }
            }
        }
        else if (type instanceof RegisterType regType) {
            return definedRegisters.get(regType.getDefinition());
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    private final class Generation {
        private final ImmutableMap<TypeDefinitionStatement, JavaEnum> mDeclaredEnums;
        private final ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> mEnumsDeclaredInMethodScope;

        Generation(
                ImmutableMap<TypeDefinitionStatement, JavaEnum> declaredEnums,
                ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> enumsDeclaredInMethodScope) {
            ensureNonNull(declaredEnums, enumsDeclaredInMethodScope);
            mDeclaredEnums = declaredEnums;
            mEnumsDeclaredInMethodScope = enumsDeclaredInMethodScope;
        }

        private JavaExpression generateExpression(
                Expression expression,
                Set<JavaEnum> enumsInScope) {
            if (expression instanceof AdditionExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaAdditionExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof AndExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                        mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
                return new JavaAndExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof ArrayConcatenationExpression exp) {
                return new JavaArrayConcatenationExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof ArrayValueAtExpression exp) {
                return new JavaArrayValueAtExpression(
                        generateExpression(exp.getArray(), enumsInScope),
                        generateExpression(exp.getIndex(), enumsInScope));
            }
            else if (expression instanceof DivisionExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaDivisionExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof EnumValueLiteralExpression exp) {
                final String enumValue = exp.getValue();
                for (JavaEnum javaEnum : enumsInScope) {
                    final int index = javaEnum.getValues().indexWhere(v -> v.getName().equals(enumValue));
                    if (index >= 0) {
                        return new JavaEnumValueLiteralExpression(javaEnum.getValues().valueAt(index));
                    }
                }
                throw new RuntimeException("Unknown enum value '" + enumValue + "'");
            }
            else if (expression instanceof EqualThanExpression exp) {
                final Type leftType = mTypeMap.get(exp.getLeftExpression());
                if (leftType instanceof IntType || leftType instanceof EnumType) {
                    return new JavaEqualThanExpression(generateExpression(exp.getLeftExpression(), enumsInScope), generateExpression(exp.getRightExpression(), enumsInScope));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                if (exp.getFunction() instanceof ReferenceExpression funcRef) {
                    return new JavaFunctionExecutionExpression(funcRef.getReference(), exp.getParameters().map(p -> generateExpression(p, enumsInScope)));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof GreaterOrEqualThanExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaGreaterOrEqualThanExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof IfExpression exp) {
                ensureValidState(mTypeMap.get(exp.getCondition()) instanceof EnumType enumType && enumType.isBooleanType());
                return new JavaTernaryExpression(
                        generateExpression(exp.getCondition(), enumsInScope),
                        generateExpression(exp.getThenClause(), enumsInScope),
                        generateExpression(exp.getElseClause(), enumsInScope));
            }
            else if (expression instanceof IntegerLiteralExpression exp) {
                return new JavaIntegerLiteralExpression(exp.getLiteral());
            }
            else if (expression instanceof LowerThanExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaLowerThanExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof ModuleExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaModuleExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof MultiplicationExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaMultiplicationExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof OrExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                        mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
                return new JavaOrExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope));
            }
            else if (expression instanceof ReferenceExpression exp) {
                return new JavaReferenceExpression(exp.getReference());
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                // TODO: Fix this once we have constructors
                return new JavaNewExpression();
            }
            else if (expression instanceof RegisterFieldAccessExpression exp) {
                final Type type = mTypeMap.get(exp.getRegister());
                if (type instanceof ArrayType && exp.getFieldName().equals(RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH)) {
                    return new JavaArrayLengthExpression(generateExpression(exp.getRegister(), enumsInScope));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof StringLiteralExpression exp) {
                return new JavaStringLiteralExpression(exp.getLiteral());
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }

        private ImmutableList<JavaStatement> generateMethodCode(
                Expression expression,
                Set<JavaEnum> enumsInScope) {
            if (expression instanceof ArrayConcatenationExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope)))
                        .build();
            }
            else if (expression instanceof ComplexExpression exp) {
                // TODO: We should handle the statements as well. For now we just bypass the expression inside
                return generateMethodCode(exp.getExpression(), enumsInScope);
            }
            else if (expression instanceof EnumValueLiteralExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope)))
                        .build();
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope)))
                        .build();
            }
            else if (expression instanceof IfExpression exp) {
                final JavaExpression condition = generateExpression(exp.getCondition(), enumsInScope);
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaIfStatement(condition,
                                generateMethodCode(exp.getThenClause(), enumsInScope),
                                generateMethodCode(exp.getElseClause(), enumsInScope)))
                        .build();
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope)))
                        .build();
            }
            else if (expression instanceof StringLiteralExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope)))
                        .build();
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
    }

    public ImmutableSet<JavaClass> generate(ImmutableList<Statement> statements) {
        final ImmutableSet<TypeDefinitionStatement> allTypeDefinitionStatements = findAllTypeDefinitions(statements);
        final MutableSet<JavaClassReference> inUse = MutableHashSet.empty();
        inUse.add(mOutputClassRef);

        final JavaPackage javaPackage = mOutputClassRef.getPackage();
        final ImmutableMap<EnumType.Definition, JavaClassReference> allEnums = buildEnumsMap(javaPackage, allTypeDefinitionStatements, inUse);
        inUse.addAll(allEnums.toList());

        final ImmutableMap<RegisterType.Definition, JavaClassReference> allRegisters = buildRegistersMap(javaPackage, allTypeDefinitionStatements, inUse);
        inUse.addAll(allRegisters.toList());

        final MutableMap<TypeDefinitionStatement, JavaEnum> allEnumClasses = MutableHashMap.empty();
        for (TypeDefinitionStatement statement : allTypeDefinitionStatements) {
            if (statement.getType() instanceof EnumType enumType) {
                final EnumType.Definition enumDef = enumType.getDefinition();
                final JavaClassReference classRef = allEnums.get(enumDef);
                final int classModifiers = statement.isPublic()? JavaNormalClass.Modifiers.PUBLIC : 0;
                final JavaEnum cls = classRef.newEnum(classModifiers);
                allEnumClasses.put(statement, cls);

                for (String value : enumDef.getPossibleValues()) {
                    cls.addValue(value);
                }
            }
        }

        final MutableList<JavaNormalClass> allNormalClasses = MutableList.empty();
        for (TypeDefinitionStatement statement : allTypeDefinitionStatements) {
            if (statement.getType() instanceof RegisterType regType) {
                final RegisterType.Definition regDef = regType.getDefinition();
                final JavaClassReference classRef = allRegisters.get(regDef);
                final int classModifiers = statement.isPublic()? JavaNormalClass.Modifiers.PUBLIC | JavaNormalClass.Modifiers.FINAL : JavaNormalClass.Modifiers.FINAL;
                final JavaNormalClass cls = classRef.newClass(classModifiers);
                allNormalClasses.append(cls);

                for (Map.Entry<String, Type> fieldEntry : regDef.getFields().entries()) {
                    final int modifiers = JavaField.Modifiers.PRIVATE | JavaField.Modifiers.FINAL;
                    cls.addField(mCodeStyleForRegister.propertyName(fieldEntry.key()), transformToJavaType(fieldEntry.value(), allEnums, allRegisters), modifiers);
                }

                final ImmutableList<JavaVariable> constructorParameters = regDef.getFields().entries().map(fieldEntry ->
                        new JavaVariable(fieldEntry.key(), transformToJavaType(fieldEntry.value(), allEnums, allRegisters)));
                final JavaReferenceExpression thisExpression = new JavaReferenceExpression("this");
                final ImmutableList<JavaStatement> constructorStatements = regDef.getFields().entries().map(fieldEntry ->
                        new JavaAssignmentStatement(new JavaFieldAccessExpression(thisExpression, fieldEntry.key()), new JavaReferenceExpression(fieldEntry.key())));
                cls.addConstructor(constructorParameters, classModifiers, constructorStatements);
            }
        }

        final JavaNormalClass outputClass = mOutputClassRef.newClass(JavaNormalClass.Modifiers.PUBLIC | JavaNormalClass.Modifiers.FINAL);
        allNormalClasses.append(outputClass);

        final MethodFinder methodFinder = new MethodFinder(allEnumClasses.toImmutable());
        final ImmutableMap<ConstantDefinitionStatement, ImmutableSet<JavaEnum>> allStatementMethods = methodFinder.findAllMethods(statements, ImmutableHashSet.empty());
        final ImmutableMap<FunctionDefinitionExpression, JavaMethodReference> allMethods = buildMethodsMap(outputClass, allStatementMethods.keySet(), allEnums, allRegisters);

        final Generation generation = new Generation(allEnumClasses.toImmutable(), allStatementMethods);
        for (ConstantDefinitionStatement constDef : allStatementMethods.keySet()) {
            final FunctionDefinitionExpression funcDef = (FunctionDefinitionExpression) constDef.getExpression();
            final JavaMethodReference methodRef = allMethods.get(funcDef);
            final int methodModifiers = JavaNormalClass.Modifiers.STATIC |
                    (constDef.isPublic()? JavaNormalClass.Modifiers.PUBLIC : JavaNormalClass.Modifiers.PRIVATE);

            final ImmutableList<JavaStatement> javaStatements = generation.generateMethodCode(funcDef.getBody(), allStatementMethods.get(constDef));
            outputClass.addMethod(methodRef, funcDef.getParameters().map(FunctionParameter::getName), methodModifiers, javaStatements);
        }

        final ImmutableSet.Builder<JavaClass> builder = new ImmutableHashSet.Builder<>();
        for (JavaEnum enumClass : allEnumClasses) {
            builder.add(enumClass);
        }

        for (JavaNormalClass cls : allNormalClasses) {
            builder.add(cls);
        }

        return builder.build();
    }
}