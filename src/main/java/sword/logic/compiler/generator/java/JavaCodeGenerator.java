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
import sword.logic.compiler.generator.java.entities.JavaConstructor;
import sword.logic.compiler.generator.java.entities.JavaEnum;
import sword.logic.compiler.generator.java.entities.JavaField;
import sword.logic.compiler.generator.java.entities.JavaMethod;
import sword.logic.compiler.generator.java.entities.JavaMethodReference;
import sword.logic.compiler.generator.java.entities.JavaNormalClass;
import sword.logic.compiler.generator.java.entities.JavaPackage;
import sword.logic.compiler.generator.java.entities.JavaVariable;
import sword.logic.compiler.generator.java.expressions.JavaAdditionExpression;
import sword.logic.compiler.generator.java.expressions.JavaAndExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayConcatenationExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayConstructionExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayLengthExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayValueAtExpression;
import sword.logic.compiler.generator.java.expressions.JavaDifferentFromExpression;
import sword.logic.compiler.generator.java.expressions.JavaDivisionExpression;
import sword.logic.compiler.generator.java.expressions.JavaEnumValueLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaExpression;
import sword.logic.compiler.generator.java.expressions.JavaFalseLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaFieldAccessExpression;
import sword.logic.compiler.generator.java.expressions.JavaGreaterOrEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaIntegerLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaLambdaExpression;
import sword.logic.compiler.generator.java.expressions.JavaLowerThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaMethodExecutionExpression;
import sword.logic.compiler.generator.java.expressions.JavaModuleExpression;
import sword.logic.compiler.generator.java.expressions.JavaMultiplicationExpression;
import sword.logic.compiler.generator.java.expressions.JavaNewExpression;
import sword.logic.compiler.generator.java.expressions.JavaOrExpression;
import sword.logic.compiler.generator.java.expressions.JavaReferenceExpression;
import sword.logic.compiler.generator.java.expressions.JavaStringLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaSubtractionExpression;
import sword.logic.compiler.generator.java.expressions.JavaTernaryExpression;
import sword.logic.compiler.generator.java.expressions.JavaTrueLiteralExpression;
import sword.logic.compiler.generator.java.statements.JavaAssignmentStatement;
import sword.logic.compiler.generator.java.statements.JavaConstantDeclarationStatement;
import sword.logic.compiler.generator.java.statements.JavaConstantDefinitionStatement;
import sword.logic.compiler.generator.java.statements.JavaIfStatement;
import sword.logic.compiler.generator.java.statements.JavaReturnStatement;
import sword.logic.compiler.generator.java.statements.JavaStatement;
import sword.logic.compiler.generator.java.types.JavaBooleanType;
import sword.logic.compiler.generator.java.types.JavaIntType;
import sword.logic.compiler.generator.java.types.JavaType;
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
import sword.logic.types.FunctionType;
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

    private final JavaClassReference mListClassReference;
    private final JavaClassReference mIntListClassReference;
    private final JavaPackage mFunctionClassPackage;
    private final MutableMap<String, JavaClassReference> mFunctionClassReferences = MutableHashMap.empty();

    public JavaCodeGenerator(
            JavaClassReference outputClassReference,
            ImmutableMap<Expression, Type> expressionTypeMap) {
        mOutputClassRef = outputClassReference;
        mTypeMap = expressionTypeMap;

        final JavaPackage collectionsPackage = mOutputClassRef.getPackage();
        mListClassReference = collectionsPackage.newClassReference("ImmutableList");
        mIntListClassReference = collectionsPackage.newClassReference("ImmutableIntList");
        mFunctionClassPackage = mOutputClassRef.getPackage();
    }

    private JavaClassReference obtainFunctionClassReference(String signature) {
        JavaClassReference ref = mFunctionClassReferences.get(signature, null);
        if (ref == null) {
            ref = mFunctionClassPackage.newClassReference("Function" + signature);
            mFunctionClassReferences.put(signature, ref);
        }

        return ref;
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

    private static final class MethodFinder {
        private final ImmutableMap<TypeDefinitionStatement, JavaEnum> mDeclaredEnums;
        private final ImmutableMap<TypeDefinitionStatement, JavaConstructor> mDeclaredRegisterConstructors;

        MethodFinder(
                ImmutableMap<TypeDefinitionStatement, JavaEnum> declaredEnums,
                ImmutableMap<TypeDefinitionStatement, JavaConstructor> declaredRegisterConstructors) {
            ensureNonNull(declaredEnums, declaredRegisterConstructors);
            mDeclaredEnums = declaredEnums;
            mDeclaredRegisterConstructors = declaredRegisterConstructors;
        }

        private ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> findAllMethods(Expression expression, ImmutableSet<JavaEnum> enumsInScope, ImmutableMap<String, JavaConstructor> registerConstructorsInScope) {
            if (expression instanceof ArrayConstructionExpression exp) {
                ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> result = ImmutableHashMap.empty();
                for (Expression value : exp.getValues()) {
                    result = result.putAll(findAllMethods(value, enumsInScope, registerConstructorsInScope));
                }

                return result;
            }
            else if (expression instanceof ArrayValueAtExpression exp) {
                return findAllMethods(exp.getArray(), enumsInScope, registerConstructorsInScope)
                        .putAll(findAllMethods(exp.getIndex(), enumsInScope, registerConstructorsInScope));
            }
            else if (expression instanceof ComplexExpression exp) {
                final ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> statementsResult = findAllMethods(exp.getStatements(), enumsInScope, registerConstructorsInScope);

                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof TypeDefinitionStatement typeDef) {
                        final JavaEnum statementEnum = mDeclaredEnums.get(typeDef, null);
                        if (statementEnum != null) {
                            enumsInScope = enumsInScope.add(statementEnum);
                        }
                        else {
                            final JavaConstructor statementRegConstructor = mDeclaredRegisterConstructors.get(typeDef, null);
                            if (statementRegConstructor != null) {
                                registerConstructorsInScope = registerConstructorsInScope.put(statement.getName(), statementRegConstructor);
                            }
                        }
                    }
                }

                return statementsResult.putAll(findAllMethods(exp.getExpression(), enumsInScope, registerConstructorsInScope));
            }
            else if (expression instanceof FunctionDefinitionExpression exp) {
                return findAllMethods(exp.getBody(), enumsInScope, registerConstructorsInScope);
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> result = findAllMethods(exp.getFunction(), enumsInScope, registerConstructorsInScope);
                for (Expression value : exp.getParameters()) {
                    result = result.putAll(findAllMethods(value, enumsInScope, registerConstructorsInScope));
                }

                return result;
            }
            else if (expression instanceof IfExpression exp) {
                return findAllMethods(exp.getCondition(), enumsInScope, registerConstructorsInScope)
                        .putAll(findAllMethods(exp.getThenClause(), enumsInScope, registerConstructorsInScope))
                        .putAll(findAllMethods(exp.getElseClause(), enumsInScope, registerConstructorsInScope));
            }
            else if (expression instanceof LeftRightExpression exp) {
                return findAllMethods(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope)
                        .putAll(findAllMethods(exp.getRightExpression(), enumsInScope, registerConstructorsInScope));
            }
            else if (expression instanceof LiteralExpression) {
                return ImmutableHashMap.empty();
            }
            else if (expression instanceof ReferenceExpression) {
                return ImmutableHashMap.empty();
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                return findAllMethods(exp.getStatements(), enumsInScope, registerConstructorsInScope);
            }
            else if (expression instanceof RegisterFieldAccessExpression exp) {
                return findAllMethods(exp.getRegister(), enumsInScope, registerConstructorsInScope);
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }

        private static final class ScopeDefinitions {
            private final ImmutableSet<JavaEnum> mEnums;
            private final ImmutableMap<String, JavaConstructor> mRegisterConstructors;

            ScopeDefinitions(
                    ImmutableSet<JavaEnum> enums,
                    ImmutableMap<String, JavaConstructor> registerConstructors) {
                ensureNonNull(enums, registerConstructors);
                mEnums = enums;
                mRegisterConstructors = registerConstructors;
            }

            public ImmutableSet<JavaEnum> getEnums() {
                return mEnums;
            }

            public ImmutableMap<String, JavaConstructor> getRegisterConstructors() {
                return mRegisterConstructors;
            }
        }

        private ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> findAllMethods(ImmutableList<Statement> statements, ImmutableSet<JavaEnum> enumsInScope, ImmutableMap<String, JavaConstructor> registerConstructorsInScope) {
            ImmutableMap<ConstantDefinitionStatement, ScopeDefinitions> result = ImmutableHashMap.empty();

            for (Statement statement : statements) {
                if (statement instanceof TypeDefinitionStatement typeDef) {
                    final JavaEnum statementEnum = mDeclaredEnums.get(typeDef, null);
                    if (statementEnum != null) {
                        enumsInScope = enumsInScope.add(statementEnum);
                    }
                    else {
                        final JavaConstructor statementRegConstructor = mDeclaredRegisterConstructors.get(typeDef, null);
                        if (statementRegConstructor != null) {
                            registerConstructorsInScope = registerConstructorsInScope.put(statement.getName(), statementRegConstructor);
                        }
                    }
                }
            }

            final ScopeDefinitions scopeDefinitions = new ScopeDefinitions(
                    enumsInScope.toImmutable(),
                    registerConstructorsInScope.toImmutable());

            for (Statement statement : statements) {
                if (statement instanceof ConstantDefinitionStatement constDef) {
                    if (constDef.getExpression() instanceof FunctionDefinitionExpression) {
                        result = result.put(constDef, scopeDefinitions);
                    }

                    result = result.putAll(findAllMethods(constDef.getExpression(), enumsInScope, registerConstructorsInScope));
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
            else if (arrayType.getItemType() instanceof IntType) {
                return mIntListClassReference;
            }
            else {
                return mListClassReference.newGenericClassReference(new ImmutableList.Builder<JavaType>()
                        .append(transformToJavaType(arrayType.getItemType(), definedEnums, definedRegisters))
                        .build());
            }
        }
        else if (type instanceof FunctionType funcType) {
            final ImmutableList<JavaType> types = funcType.getParameterTypes()
                    .map(t -> transformToJavaType(t, definedEnums, definedRegisters))
                    .append(transformToJavaType(funcType.getResultType(), definedEnums, definedRegisters));

            final ImmutableList.Builder<JavaType> genericsBuilder = new ImmutableList.Builder<>();
            for (JavaType t : types) {
                if ("L".equals(t.getSignatureType())) {
                    genericsBuilder.append(t);
                }
            }

            final JavaClassReference ref = obtainFunctionClassReference(types
                    .map(JavaType::getSignatureType)
                    .reduce((a, b) -> a + b));

            final ImmutableList<JavaType> generics = genericsBuilder.build();
            return generics.isEmpty()? ref : ref.newGenericClassReference(generics);
        }
        else if (type instanceof IntType intType) {
            final String min = intType.getMin();
            final String max = intType.getMax();
            if (min.equals(TypeConstants.unboundText) || max.equals(TypeConstants.unboundText)) {
                throw new UnsupportedOperationException("Unimplemented");
            }
            else {
                return JavaIntType.getInstance();
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
        private final ImmutableMap<ConstantDefinitionStatement, MethodFinder.ScopeDefinitions> mMethodScopeDefinitions;
        private final ImmutableMap<EnumType.Definition, JavaClassReference> mDefinedEnums;
        private final ImmutableMap<RegisterType.Definition, JavaClassReference> mDefinedRegisters;

        Generation(
                ImmutableMap<TypeDefinitionStatement, JavaEnum> declaredEnums,
                ImmutableMap<ConstantDefinitionStatement, MethodFinder.ScopeDefinitions> methodScopeDefinitions,
                ImmutableMap<EnumType.Definition, JavaClassReference> definedEnums,
                ImmutableMap<RegisterType.Definition, JavaClassReference> definedRegisters) {
            ensureNonNull(declaredEnums, methodScopeDefinitions, definedEnums, definedRegisters);
            mDeclaredEnums = declaredEnums;
            mMethodScopeDefinitions = methodScopeDefinitions;
            mDefinedEnums = definedEnums;
            mDefinedRegisters = definedRegisters;
        }

        private JavaExpression generateExpression(
                Expression expression,
                Set<JavaEnum> enumsInScope,
                Map<String, JavaConstructor> registerConstructorsInScope,
                Set<String> lambdaMethods,
                Map<String, JavaExpression> methodsInScope) {
            if (expression instanceof AdditionExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaAdditionExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof AndExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                        mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
                return new JavaAndExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof ArrayConcatenationExpression exp) {
                return new JavaArrayConcatenationExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof ArrayConstructionExpression exp) {
                return new JavaArrayConstructionExpression(transformToJavaType(((ArrayType) mTypeMap.get(exp)).getItemType(), mDefinedEnums, mDefinedRegisters), exp.getValues().map(e -> generateExpression(e, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)));
            }
            else if (expression instanceof ArrayValueAtExpression exp) {
                return new JavaArrayValueAtExpression(
                        generateExpression(exp.getArray(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getIndex(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof DifferentFromExpression exp) {
                final Type leftType = mTypeMap.get(exp.getLeftExpression());
                if (leftType instanceof IntType || leftType instanceof EnumType) {
                    return new JavaDifferentFromExpression(
                            generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                            generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof DivisionExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaDivisionExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof EnumValueLiteralExpression exp) {
                final String enumValue = exp.getValue();
                for (JavaEnum javaEnum : enumsInScope) {
                    final int index = javaEnum.getValues().indexWhere(v -> v.getName().equals(enumValue));
                    if (index >= 0) {
                        return new JavaEnumValueLiteralExpression(javaEnum.getValues().valueAt(index));
                    }
                }

                if ("TRUE".equals(enumValue)) {
                    return JavaTrueLiteralExpression.getInstance();
                }
                else if ("FALSE".equals(enumValue)) {
                    return JavaFalseLiteralExpression.getInstance();
                }
                else {
                    throw new RuntimeException("Unknown enum value '" + enumValue + "'");
                }
            }
            else if (expression instanceof EqualThanExpression exp) {
                final Type leftType = mTypeMap.get(exp.getLeftExpression());
                if (leftType instanceof IntType || leftType instanceof EnumType) {
                    return new JavaEqualThanExpression(
                            generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                            generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof FunctionDefinitionExpression exp) {
                return new JavaLambdaExpression(
                        exp.getParameters().map(FunctionParameter::getName),
                        generateExpression(exp.getBody(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                if (exp.getFunction() instanceof ReferenceExpression funcRef) {
                    // TODO: Handle static methods
                    final String methodName = funcRef.getReference();
                    final ImmutableList<JavaExpression> params = exp.getParameters().map(p -> generateExpression(p, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
                    if (lambdaMethods.contains(methodName)) {
                        return new JavaMethodExecutionExpression(new JavaReferenceExpression(methodName), "apply", params);
                    }
                    else {
                        final JavaExpression instance = methodsInScope.get(methodName);
                        return new JavaMethodExecutionExpression(instance, methodName, params);
                    }
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof GreaterOrEqualThanExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaGreaterOrEqualThanExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof IfExpression exp) {
                ensureValidState(mTypeMap.get(exp.getCondition()) instanceof EnumType enumType && enumType.isBooleanType());
                return new JavaTernaryExpression(
                        generateExpression(exp.getCondition(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getThenClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getElseClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof IntegerLiteralExpression exp) {
                return new JavaIntegerLiteralExpression(exp.getLiteral());
            }
            else if (expression instanceof LowerThanExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaLowerThanExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof ModuleExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaModuleExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof MultiplicationExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaMultiplicationExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof OrExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                        mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
                return new JavaOrExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else if (expression instanceof ReferenceExpression exp) {
                return new JavaReferenceExpression(exp.getReference());
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                final JavaConstructor regConstructor = registerConstructorsInScope.get(exp.getType());
                final JavaClassReference regRef = regConstructor.getJavaClass().getReference();
                final ImmutableList<JavaExpression> params = regConstructor.getParameters().map(variable -> {
                    for (Statement statement : exp.getStatements()) {
                        if (statement.getName().equals(variable.getName()) && statement instanceof ConstantDefinitionStatement constDef) {
                            return generateExpression(constDef.getExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope);
                        }
                    }

                    throw new RuntimeException("No expression for '" + variable.getName() + "'");
                });
                return new JavaNewExpression(regRef, params);
            }
            else if (expression instanceof RegisterFieldAccessExpression exp) {
                final Type type = mTypeMap.get(exp.getRegister());
                if (type instanceof ArrayType && exp.getFieldName().equals(RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH)) {
                    return new JavaArrayLengthExpression(generateExpression(exp.getRegister(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
                }
                else {
                    throw new UnsupportedOperationException("Unimplemented");
                }
            }
            else if (expression instanceof StringLiteralExpression exp) {
                return new JavaStringLiteralExpression(exp.getLiteral());
            }
            else if (expression instanceof SubtractionExpression exp) {
                ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                        mTypeMap.get(exp.getRightExpression()) instanceof IntType);
                return new JavaSubtractionExpression(
                        generateExpression(exp.getLeftExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                        generateExpression(exp.getRightExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }

        private ImmutableList<JavaStatement> generateAssignment(
                Expression expression,
                Set<JavaEnum> enumsInScope,
                Map<String, JavaConstructor> registerConstructorsInScope,
                Set<String> lambdaMethods,
                Map<String, JavaExpression> methodsInScope,
                String output) {
            if (expression instanceof ComplexExpression exp) {
                final ImmutableList.Builder<JavaStatement> builder = new ImmutableList.Builder<>();
                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof ConstantDefinitionStatement constDef) {
                        if (constDef.getExpression() instanceof IfExpression) {
                            final JavaVariable variable = new JavaVariable(constDef.getName(), transformToJavaType(mTypeMap.get(constDef.getExpression()), mDefinedEnums, mDefinedRegisters));
                            builder.append(new JavaConstantDeclarationStatement(variable));
                            for (JavaStatement st : generateAssignment(constDef.getExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope, constDef.getName())) {
                                builder.append(st);
                            }
                        }
                        else if (!(constDef.getExpression() instanceof FunctionDefinitionExpression)) {
                            final JavaVariable variable = new JavaVariable(constDef.getName(), transformToJavaType(mTypeMap.get(constDef.getExpression()), mDefinedEnums, mDefinedRegisters));
                            builder.append(new JavaConstantDefinitionStatement(variable, generateExpression(constDef.getExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)));
                        }
                    }
                }

                for (JavaStatement st : generateAssignment(exp.getExpression(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope, output)) {
                    builder.append(st);
                }

                return builder.build();
            }
            else if (expression instanceof IfExpression exp) {
                final JavaExpression condition = generateExpression(exp.getCondition(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope);
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaIfStatement(condition,
                                generateAssignment(exp.getThenClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope, output),
                                generateAssignment(exp.getElseClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope, output)))
                        .build();
            }
            else {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaAssignmentStatement(
                                new JavaReferenceExpression(output),
                                generateExpression(expression, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
        }

        private ImmutableList<JavaStatement> generateMethodCode(
                Expression expression,
                Set<JavaEnum> enumsInScope,
                Map<String, JavaConstructor> registerConstructorsInScope,
                Set<String> lambdaMethods,
                Map<String, JavaExpression> methodsInScope) {
            if (expression instanceof ArrayConcatenationExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
            else if (expression instanceof ComplexExpression exp) {
                final MutableSet<JavaEnum> newEnumsInScope = enumsInScope.mutate();
                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof TypeDefinitionStatement typeDef && typeDef.getType() instanceof EnumType) {
                        newEnumsInScope.add(mDeclaredEnums.get(typeDef));
                    }
                }

                final MutableSet<String> newLambdaMethods = lambdaMethods.mutate();
                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof ConstantDefinitionStatement constDef && constDef.getExpression() instanceof FunctionDefinitionExpression) {
                        newLambdaMethods.add(statement.getName());
                    }
                }

                final ImmutableList.Builder<JavaStatement> builder = new ImmutableList.Builder<>();
                for (Statement statement : exp.getStatements()) {
                    if (statement instanceof ConstantDefinitionStatement constDef) {
                        if (constDef.getExpression() instanceof IfExpression) {
                            final JavaVariable variable = new JavaVariable(constDef.getName(), transformToJavaType(mTypeMap.get(constDef.getExpression()), mDefinedEnums, mDefinedRegisters));
                            builder.append(new JavaConstantDeclarationStatement(variable));
                            for (JavaStatement st : generateAssignment(constDef.getExpression(), enumsInScope, registerConstructorsInScope, newLambdaMethods, methodsInScope, constDef.getName())) {
                                builder.append(st);
                            }
                        }
                        else {
                            final JavaVariable variable = new JavaVariable(constDef.getName(), transformToJavaType(mTypeMap.get(constDef.getExpression()), mDefinedEnums, mDefinedRegisters));
                            builder.append(new JavaConstantDefinitionStatement(variable, generateExpression(constDef.getExpression(), newEnumsInScope, registerConstructorsInScope, newLambdaMethods, methodsInScope)));
                        }
                    }
                }

                for (JavaStatement st : generateMethodCode(exp.getExpression(), enumsInScope, registerConstructorsInScope, newLambdaMethods, methodsInScope)) {
                    builder.append(st);
                }

                return builder.build();
            }
            else if (expression instanceof EnumValueLiteralExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
            else if (expression instanceof FunctionExecutionExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
            else if (expression instanceof IfExpression exp) {
                final JavaExpression condition = generateExpression(exp.getCondition(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope);
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaIfStatement(condition,
                                generateMethodCode(exp.getThenClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope),
                                generateMethodCode(exp.getElseClause(), enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
            else if (expression instanceof RegisterConstructionExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
                        .build();
            }
            else if (expression instanceof StringLiteralExpression exp) {
                return new ImmutableList.Builder<JavaStatement>()
                        .append(new JavaReturnStatement(generateExpression(exp, enumsInScope, registerConstructorsInScope, lambdaMethods, methodsInScope)))
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
        final MutableMap<TypeDefinitionStatement, JavaConstructor> allRegisterConstructors = MutableHashMap.empty();
        for (TypeDefinitionStatement statement : allTypeDefinitionStatements) {
            if (statement.getType() instanceof RegisterType regType) {
                final RegisterType.Definition regDef = regType.getDefinition();
                final JavaClassReference classRef = allRegisters.get(regDef);
                final int classModifiers = statement.isPublic()? JavaNormalClass.Modifiers.PUBLIC | JavaNormalClass.Modifiers.FINAL : JavaNormalClass.Modifiers.FINAL;
                final JavaNormalClass cls = classRef.newClass(classModifiers);
                allNormalClasses.append(cls);

                for (Map.Entry<String, Type> fieldEntry : regDef.getFields().entries()) {
                    final String fieldName = mCodeStyleForRegister.propertyName(fieldEntry.key());
                    final int modifiers = JavaField.Modifiers.PRIVATE | JavaField.Modifiers.FINAL;
                    final JavaType propertyType = transformToJavaType(fieldEntry.value(), allEnums, allRegisters);
                    cls.addField(fieldName, propertyType, modifiers);

                    final boolean isBooleanProperty = propertyType == JavaBooleanType.getInstance();
                    final String getterMethodName = mCodeStyleForRegister.getterName(fieldEntry.key(), isBooleanProperty);
                    final JavaMethodReference methodReference = cls.newMethodReference(getterMethodName, ImmutableList.empty(), propertyType);
                    cls.addMethod(methodReference, ImmutableList.empty(), JavaMethod.Modifiers.PUBLIC, new ImmutableList.Builder<JavaStatement>()
                            .append(new JavaReturnStatement(new JavaReferenceExpression(fieldName)))
                            .build());
                }

                final ImmutableList<JavaVariable> constructorParameters = regDef.getFields().entries().map(fieldEntry ->
                        new JavaVariable(fieldEntry.key(), transformToJavaType(fieldEntry.value(), allEnums, allRegisters)));
                final JavaReferenceExpression thisExpression = new JavaReferenceExpression("this");
                final ImmutableList<JavaStatement> constructorStatements = regDef.getFields().entries().map(fieldEntry ->
                        new JavaAssignmentStatement(new JavaFieldAccessExpression(thisExpression, fieldEntry.key()), new JavaReferenceExpression(fieldEntry.key())));
                final JavaConstructor constructor = cls.addConstructor(constructorParameters, classModifiers, constructorStatements);
                allRegisterConstructors.put(statement, constructor);
            }
        }

        final JavaNormalClass outputClass = mOutputClassRef.newClass(JavaNormalClass.Modifiers.PUBLIC | JavaNormalClass.Modifiers.FINAL);
        allNormalClasses.append(outputClass);

        final MethodFinder methodFinder = new MethodFinder(allEnumClasses.toImmutable(), allRegisterConstructors.toImmutable());
        final ImmutableMap<ConstantDefinitionStatement, MethodFinder.ScopeDefinitions> allStatementMethods = methodFinder.findAllMethods(statements, ImmutableHashSet.empty(), ImmutableHashMap.empty());
        final ImmutableMap<FunctionDefinitionExpression, JavaMethodReference> allMethods = buildMethodsMap(outputClass, allStatementMethods.keySet(), allEnums, allRegisters);

        final Generation generation = new Generation(allEnumClasses.toImmutable(), allStatementMethods, allEnums.toImmutable(), allRegisters.toImmutable());
        for (Statement statement : statements) {
            if (statement instanceof ConstantDefinitionStatement constDef && constDef.getExpression() instanceof FunctionDefinitionExpression funcDef) {
                final JavaMethodReference methodRef = allMethods.get(funcDef);
                final int methodModifiers = JavaNormalClass.Modifiers.STATIC |
                        (constDef.isPublic()? JavaNormalClass.Modifiers.PUBLIC : JavaNormalClass.Modifiers.PRIVATE);

                final MethodFinder.ScopeDefinitions scopeDefinitions = allStatementMethods.get(constDef);
                final ImmutableList<JavaStatement> javaStatements = generation.generateMethodCode(funcDef.getBody(), scopeDefinitions.getEnums(), scopeDefinitions.getRegisterConstructors(), ImmutableHashSet.empty(), ImmutableHashMap.empty());
                outputClass.addMethod(methodRef, funcDef.getParameters().map(FunctionParameter::getName), methodModifiers, javaStatements);
            }
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