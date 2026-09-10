package sword.logic.compiler.generator.c;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.c.statements.CFileRootStatement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public final class CCodeWriter {
    private String extractFilename(String path) {
        final int lastSlash = path.lastIndexOf('/');
        return (lastSlash >= 0)? path.substring(lastSlash + 1) : path;
    }

    public void writeHeader(String path, ImmutableList<CFileRootStatement> statements) {
        final String filename = extractFilename(path);
        try (FileOutputStream outStream = new FileOutputStream(path + ".h")) {
            final PrintWriter out = new PrintWriter(outStream, true);
            final String headerDefine = "_" + filename.toUpperCase() + "_H_";
            out.println("#ifndef " + headerDefine);
            out.println("#define " + headerDefine);

            final Formatter formatter = new DefaultFormatter();
            for (CFileRootStatement statement : statements) {
                out.println();
                out.println(statement.getText(formatter));
            }
            out.println();
            out.println("#endif /* " + headerDefine + " */");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeSource(String path, ImmutableList<CFileRootStatement> statements) {
        final String filename = extractFilename(path);
        try (FileOutputStream outStream = new FileOutputStream(path + ".c")) {
            final PrintWriter out = new PrintWriter(outStream, true);
            out.println("#include \"" + filename + ".h\"");
            out.println("#include <string.h>\n"); // Required for memcpy

            final Formatter formatter = new DefaultFormatter();
            for (CFileRootStatement statement : statements) {
                out.println();
                out.println(statement.getText(formatter));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
