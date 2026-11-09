package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public final class JavaCodeWriter {
    public void writeSource(String outputDir, ImmutableList<String> outputPackage, String outputClassName) {
        File directory = new File(outputDir);
        if (directory.exists()) {
            for (String subfolder : outputPackage) {
                directory = new File(directory, subfolder);
                if (!directory.exists() && !directory.mkdir()) {
                    throw new RuntimeException("Unable to create directory " + directory);
                }
            }
        }

        final String path = outputDir + File.separator + outputPackage.map(s -> s + File.separator).reduce((a, b) -> a + b, "") + outputClassName + ".java";
        try (FileOutputStream outStream = new FileOutputStream(path)) {
            final PrintWriter out = new PrintWriter(outStream, true);
            out.println("package " + outputPackage.reduce((a, b) -> a + "." + b) + ";");
            out.println();
            out.println("public final class " + outputClassName + " {");
            out.println("    private " + outputClassName + "() {");
            out.println("    }");
            out.println("}");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
