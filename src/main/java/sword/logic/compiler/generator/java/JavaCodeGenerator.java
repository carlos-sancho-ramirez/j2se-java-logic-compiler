package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableHashMap;
import sword.collections.MutableList;
import sword.collections.MutableMap;
import sword.logic.compiler.IntegerLiteralOperations;
import sword.logic.expressions.Expression;
import sword.logic.expressions.FunctionDefinitionExpression;
import sword.logic.statements.ConstantDefinitionStatement;
import sword.logic.statements.Statement;
import sword.logic.statements.TypeDefinitionStatement;
import sword.logic.types.ArrayType;
import sword.logic.types.EnumType;
import sword.logic.types.IntType;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;
import sword.logic.types.TypeConstants;

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
                    outputClass.addMethod(statement.getName(), parameters, transformToJavaType(mTypeMap.get(funcDef.getBody()), definedRegisters), methodModifiers);
                }
            }
        }

        return allClasses.toImmutable();
    }
}