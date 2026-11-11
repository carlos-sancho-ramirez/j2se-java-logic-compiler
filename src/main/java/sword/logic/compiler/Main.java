package sword.logic.compiler;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.CCodeGenerator;
import sword.logic.compiler.generator.c.CCodeWriter;
import sword.logic.compiler.generator.java.JavaClass;
import sword.logic.compiler.generator.java.JavaClassReference;
import sword.logic.compiler.generator.java.JavaCodeGenerator;
import sword.logic.compiler.generator.java.JavaCodeWriter;
import sword.logic.compiler.generator.java.JavaPackage;
import sword.logic.interpreter.LogicInterpreter;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidState;

/**
 * Reads the given file and creates a c and h file ready to be compiled by a C compiler.
 * <p>
 * Steps that this compiler will perform:
 * 1. It will read from an input stream the source code and will create an Abstract tree representing the code.
 *     During this process, any syntax or semantic error must be reported to the user. It will not be possible after this step.
 *     This ensures that the resulting tree will not need to store information regarding where certain symbols were declared in the source file.
 *     This will also perform optimization steps when possible, reporting any warning to the user.
 * 2. It will traverse the abstract tree to generate another tree reflecting the result code in C.
 * 3. It will generate the header file according to the C language tree.
 *     All the information required for this step must be stored in the C abstract tree.
 * 4. It will generate the source file according to the C language tree.
 *     All the information required for this step must be stored in the C abstract tree.
 */
public final class Main {
    private enum Language {
        C, JAVA
    }

    private static final class Parameters {
        final String sourceFilePath;
        final Language outputLanguage;

        Parameters(String sourceFilePath, Language outputLanguage) {
            ensureNonNull(sourceFilePath, outputLanguage);
            this.sourceFilePath = sourceFilePath;
            this.outputLanguage = outputLanguage;
        }
    }

    private static Parameters parseParameters(String[] args) {
        final int argCount = args.length;
        String sourceFilePath = null;
        Language outputLanguage = null;

        for (int argIndex = 0; argIndex < argCount; argIndex++) {
            final String arg = args[argIndex];
            if ("--outputLanguage".equals(arg)) {
                if (argIndex == argCount - 1) {
                    System.err.println("Expected language after argument --outputLanguage");
                    return null;
                }
                else if (outputLanguage != null) {
                    System.err.println("Argument --outputLanguage present more than once");
                    return null;
                }
                else {
                    final String languageArg = args[++argIndex];
                    if ("c".equals(languageArg)) {
                        outputLanguage = Language.C;
                    }
                    else if ("java".equals(languageArg)) {
                        outputLanguage = Language.JAVA;
                    }
                    else {
                        System.err.println("Unknown language after --outputLanguage argument");
                        return null;
                    }
                }
            }
            else if (sourceFilePath == null) {
                sourceFilePath = arg;
            }
            else {
                System.err.println("Only one source file is allowed");
                return null;
            }
        }

        if (sourceFilePath == null) {
            System.err.println("Source file path missing");
            return null;
        }
        else {
            if (outputLanguage == null) {
                outputLanguage = Language.C;
            }

            return new Parameters(sourceFilePath, outputLanguage);
        }
    }

    public static void main(String[] args) {
        final Parameters parameters = parseParameters(args);
        if (parameters != null) {
            try (InputStream inStream = new FileInputStream(parameters.sourceFilePath)) {
                final TokenParser parser = new TokenParser(inStream);
                final LogicInterpreter interpreter = new LogicInterpreter(parser);
                final LogicInterpreter.Result interpreterResult = interpreter.interpret();
                System.out.println("File read until the end with " + interpreterResult.getStatements().size() + " statements");

                if (parameters.outputLanguage == Language.JAVA) {
                    final JavaClassReference outputClassRef = JavaPackage.createPackage("com")
                            .newSubpackage("example")
                            .newClassReference("Output");
                    final JavaCodeGenerator codeGenerator = new JavaCodeGenerator(outputClassRef, interpreterResult.getExpressionTypeMap());
                    final ImmutableList<JavaClass> javaClasses = codeGenerator.generate(interpreterResult.getStatements());

                    for (JavaClass javaClass : javaClasses) {
                        final JavaCodeWriter codeWriter = new JavaCodeWriter();
                        codeWriter.writeSource("build", javaClass);
                    }
                }
                else {
                    ensureValidState(parameters.outputLanguage == Language.C);
                    final CCodeGenerator codeGenerator = new CCodeGenerator(interpreterResult.getExpressionTypeMap());
                    final CCodeGenerator.Result codeGenerationResult = codeGenerator.generate(interpreterResult.getStatements());

                    final CCodeWriter codeWriter = new CCodeWriter();
                    final String path = "build" + File.separator + "output";
                    codeWriter.writeHeader(path, codeGenerationResult.getHeaderEnums(), codeGenerationResult.getHeaderStatements());
                    codeWriter.writeSource(path, codeGenerationResult.getSourceEnums(), codeGenerationResult.getSourceStatements());
                }
            }
            catch (UnexpectedEndOfFileException e) {
                System.err.println("Unexpected end of file " + parameters.sourceFilePath + ". " + e.getMessage());
            }
            catch (SyntaxErrorException e) {
                System.err.println(e.getMessage() + " at " + parameters.sourceFilePath + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (SemanticErrorException e) {
                System.err.println(e.getMessage() + " at " + parameters.sourceFilePath + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedReferenceException e) {
                System.err.println(e.getMessage() + " at " + parameters.sourceFilePath + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (IOException e) {
                System.err.println("Unable to read file " + parameters.sourceFilePath);
            }
            catch (UnresolvedTypeReferenceException e) {
                System.err.println(e.getMessage() + " at " + parameters.sourceFilePath + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedEnumValueException e) {
                System.err.println(e.getMessage() + " at " + parameters.sourceFilePath + " " + e.getLine() + ":" + e.getColumn());
            }
        }
    }
}