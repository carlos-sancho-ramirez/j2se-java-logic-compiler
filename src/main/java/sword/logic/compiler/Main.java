package sword.logic.compiler;

import sword.logic.compiler.generator.c.CCodeGenerator;
import sword.logic.compiler.generator.c.CCodeWriter;
import sword.logic.interpreter.LogicInterpreter;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing source code file reference");
        }
        else {
            final String fileName = args[0];
            try (InputStream inStream = new FileInputStream(fileName)) {
                final TokenParser parser = new TokenParser(inStream);
                final LogicInterpreter interpreter = new LogicInterpreter(parser);
                final LogicInterpreter.Result interpreterResult = interpreter.interpret();
                System.out.println("File read until the end with " + interpreterResult.getStatements().size() + " statements");

                final CCodeGenerator codeGenerator = new CCodeGenerator(interpreterResult.getExpressionTypeMap());
                final CCodeGenerator.Result codeGenerationResult = codeGenerator.generate(interpreterResult.getStatements());

                final CCodeWriter codeWriter = new CCodeWriter();
                final String path = "build" + File.separator + "output";
                codeWriter.writeHeader(path, codeGenerationResult.getHeaderStatements());
                codeWriter.writeSource(path, codeGenerationResult.getSourceStatements());
            }
            catch (UnexpectedEndOfFileException e) {
                System.err.println("Unexpected end of file " + fileName + ". " + e.getMessage());
            }
            catch (SyntaxErrorException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (SemanticErrorException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedReferenceException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (IOException e) {
                System.err.println("Unable to read file " + fileName);
            }
            catch (UnresolvedTypeReferenceException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedEnumValueException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
        }
    }
}