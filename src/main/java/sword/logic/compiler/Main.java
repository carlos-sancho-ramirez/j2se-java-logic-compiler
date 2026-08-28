package sword.logic.compiler;

import sword.logic.interpreter.LogicInterpreter;
import sword.logic.interpreter.UnresolvedEnumValueException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
                interpreter.interpret();
                System.out.println("LogicInterpreter finalized without error");
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
            catch (UnresolvedTypeReferenceException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedReferenceException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (UnresolvedEnumValueException e) {
                System.err.println(e.getMessage() + " at " + fileName + " " + e.getLine() + ":" + e.getColumn());
            }
            catch (IOException e) {
                System.err.println("Unable to read file " + fileName);
            }
        }
    }
}