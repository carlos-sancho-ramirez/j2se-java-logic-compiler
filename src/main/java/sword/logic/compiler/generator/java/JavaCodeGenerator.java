package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableList;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.compiler.generator.java.expressions.JavaAdditionExpression;
import sword.logic.compiler.generator.java.expressions.JavaAndExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayConcatenationExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayLengthExpression;
import sword.logic.compiler.generator.java.expressions.JavaArrayValueAtExpression;
import sword.logic.compiler.generator.java.expressions.JavaDivisionExpression;
import sword.logic.compiler.generator.java.expressions.JavaEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaExpression;
import sword.logic.compiler.generator.java.expressions.JavaFunctionExecutionExpression;
import sword.logic.compiler.generator.java.expressions.JavaGreaterOrEqualThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaIntegerLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaLowerThanExpression;
import sword.logic.compiler.generator.java.expressions.JavaModuleExpression;
import sword.logic.compiler.generator.java.expressions.JavaMultiplicationExpression;
import sword.logic.compiler.generator.java.expressions.JavaOrExpression;
import sword.logic.compiler.generator.java.expressions.JavaReferenceExpression;
import sword.logic.compiler.generator.java.expressions.JavaStringLiteralExpression;
import sword.logic.compiler.generator.java.expressions.JavaTernaryExpression;
import sword.logic.compiler.generator.java.statements.JavaIfStatement;
import sword.logic.compiler.generator.java.statements.JavaReturnStatement;
import sword.logic.compiler.generator.java.statements.JavaStatement;
import sword.logic.expressions.AdditionExpression;
import sword.logic.expressions.AndExpression;
import sword.logic.expressions.ArrayConcatenationExpression;
import sword.logic.expressions.ArrayValueAtExpression;
import sword.logic.expressions.ComplexExpression;
import sword.logic.expressions.DivisionExpression;
import sword.logic.expressions.EqualThanExpression;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.expressions.FunctionExecutionExpression;
import sword.logic.expressions.GreaterOrEqualThanExpression;
import sword.logic.expressions.IfExpression;
import sword.logic.expressions.IntegerLiteralExpression;
import sword.logic.expressions.LowerThanExpression;
import sword.logic.expressions.ModuleExpression;
import sword.logic.expressions.MultiplicationExpression;
import sword.logic.expressions.OrExpression;
import sword.logic.expressions.ReferenceExpression;
import sword.logic.expressions.RegisterFieldAccessExpression;
import sword.logic.expressions.StringLiteralExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;
import sword.logic.statements.TypeDefinitionStatement;
import sword.logic.types.ArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.IntType;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;
import sword.logic.types.TypeConstants;

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

    private JavaType transformToJavaType(Type type, Map<RegisterType.Definition, JavaClassReference> definedRegisters) {
        if (type instanceof EnumType enumType && enumType.isBooleanType()) {
            return JavaBooleanType.getInstance();
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
                return new JavaArrayType(transformToJavaType(arrayType.getItemType(), definedRegisters));
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
                else {
                    throw new UnsupportedOperationException("Unimplemented");
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

    private JavaExpression generateExpression(Expression expression) {
        if (expression instanceof AdditionExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaAdditionExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof AndExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                    mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
            return new JavaAndExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof ArrayConcatenationExpression exp) {
            return new JavaArrayConcatenationExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof ArrayValueAtExpression exp) {
            return new JavaArrayValueAtExpression(
                    generateExpression(exp.getArray()),
                    generateExpression(exp.getIndex()));
        }
        else if (expression instanceof DivisionExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaDivisionExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof EqualThanExpression exp) {
            final Type leftType = mTypeMap.get(exp.getLeftExpression());
            if (leftType instanceof IntType) {
                return new JavaEqualThanExpression(generateExpression(exp.getLeftExpression()), generateExpression(exp.getRightExpression()));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            if (exp.getFunction() instanceof ReferenceExpression funcRef) {
                return new JavaFunctionExecutionExpression(funcRef.getReference(), exp.getParameters().map(this::generateExpression));
            }
            else {
                throw new UnsupportedOperationException("Unimplemented");
            }
        }
        else if (expression instanceof GreaterOrEqualThanExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaGreaterOrEqualThanExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof IfExpression exp) {
            ensureValidState(mTypeMap.get(exp.getCondition()) instanceof EnumType enumType && enumType.isBooleanType());
            return new JavaTernaryExpression(
                    generateExpression(exp.getCondition()),
                    generateExpression(exp.getThenClause()),
                    generateExpression(exp.getElseClause()));
        }
        else if (expression instanceof IntegerLiteralExpression exp) {
            return new JavaIntegerLiteralExpression(exp.getLiteral());
        }
        else if (expression instanceof LowerThanExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaLowerThanExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof ModuleExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaModuleExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof MultiplicationExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof IntType &&
                    mTypeMap.get(exp.getRightExpression()) instanceof IntType);
            return new JavaMultiplicationExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof OrExpression exp) {
            ensureValidState(mTypeMap.get(exp.getLeftExpression()) instanceof EnumType leftEnumType && leftEnumType.isBooleanType() &&
                    mTypeMap.get(exp.getRightExpression()) instanceof EnumType rightEnumType && rightEnumType.isBooleanType());
            return new JavaOrExpression(
                    generateExpression(exp.getLeftExpression()),
                    generateExpression(exp.getRightExpression()));
        }
        else if (expression instanceof ReferenceExpression exp) {
            return new JavaReferenceExpression(exp.getReference());
        }
        else if (expression instanceof RegisterFieldAccessExpression exp) {
            final Type type = mTypeMap.get(exp.getRegister());
            if (type instanceof ArrayType && exp.getFieldName().equals(RegisterFieldAccessExpression.ARRAY_FIELD_LENGTH)) {
                return new JavaArrayLengthExpression(generateExpression(exp.getRegister()));
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

    private ImmutableList<JavaStatement> generateMethodCode(Expression expression) {
        if (expression instanceof ComplexExpression exp) {
            // TODO: We should handle the statements as well. For now we just bypass the expression inside
            return generateMethodCode(exp.getExpression());
        }
        else if (expression instanceof FunctionExecutionExpression exp) {
            return new ImmutableList.Builder<JavaStatement>()
                    .append(new JavaReturnStatement(generateExpression(exp)))
                    .build();
        }
        else if (expression instanceof IfExpression exp) {
            final JavaExpression condition = generateExpression(exp.getCondition());
            return new ImmutableList.Builder<JavaStatement>()
                    .append(new JavaIfStatement(condition, generateMethodCode(exp.getThenClause()), generateMethodCode(exp.getElseClause())))
                    .build();
        }
        else {
            throw new UnsupportedOperationException("Unimplemented");
        }
    }

    public ImmutableList<JavaClass> generate(ImmutableList<Statement> statements) {
        final MutableMap<RegisterType.Definition, JavaClassReference> definedRegisters = MutableHashMap.empty();
        for (Statement statement : statements) {
            if (statement instanceof TypeDefinitionStatement typeDef && typeDef.getType() instanceof RegisterType regType) {
                definedRegisters.put(regType.getDefinition(), mOutputClassRef.getPackage().newClassReference(statement.getName()));
            }
        }

        final MutableList<JavaClass> allClasses = MutableList.empty();
        for (Statement statement : statements) {
            if (statement instanceof TypeDefinitionStatement typeDef && typeDef.getType() instanceof RegisterType regType) {
                final JavaClassReference classRef = definedRegisters.get(regType.getDefinition());
                final JavaClass cls = classRef.newClass(JavaClass.Modifiers.PUBLIC | JavaClass.Modifiers.FINAL);
                allClasses.append(cls);

                for (Map.Entry<String, Type> fieldEntry : regType.getDefinition().getFields().entries()) {
                    final int modifiers = JavaField.Modifiers.PRIVATE | JavaField.Modifiers.FINAL;
                    cls.addField(mCodeStyleForRegister.propertyName(fieldEntry.key()), transformToJavaType(fieldEntry.value(), definedRegisters), modifiers);
                }
            }
        }

        final JavaClass outputClass = mOutputClassRef.newClass(JavaClass.Modifiers.PUBLIC | JavaClass.Modifiers.FINAL);
        allClasses.append(outputClass);

        for (Statement statement : statements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                if (constDef.getExpression() instanceof FunctionDefinitionExpression funcDef) {
                    final ImmutableList<JavaVariable> parameters = funcDef.getParameters().map(p -> new JavaVariable(p.getName(), transformToJavaType(p.getType(), definedRegisters)));
                    final int methodModifiers = JavaClass.Modifiers.STATIC |
                            (statement.isPublic()? JavaClass.Modifiers.PUBLIC : JavaClass.Modifiers.PRIVATE);

                    final ImmutableList<JavaStatement> javaStatements;
                    if (funcDef.getBody() instanceof ComplexExpression exp) {
                        javaStatements = generateMethodCode(exp);
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented");
                    }
                    outputClass.addMethod(statement.getName(), parameters, transformToJavaType(mTypeMap.get(funcDef.getBody()), definedRegisters), methodModifiers, javaStatements);
                }
            }
        }

        return allClasses.toImmutable();
    }
}