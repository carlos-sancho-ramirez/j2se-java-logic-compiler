package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.logic.compiler.generator.Formatter;
import sword.logic.compiler.generator.java.statements.JavaStatement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public final class JavaCodeWriter {
    private String typeToString(JavaPackage currentPackage, JavaVoidableType type) {
        if (type == JavaBooleanType.getInstance()) {
            return "boolean";
        }
        else if (type == JavaByteType.getInstance()) {
            return "byte";
        }
        else if (type instanceof JavaArrayType arrayType) {
            return typeToString(currentPackage, arrayType.getItemType()) + "[]";
        }
        else {
            final JavaClassReference ref = (JavaClassReference) type;
            final JavaPackage refPackage = ref.getPackage();
            final String refPackageStr = refPackage.toStringList().reduce((a, b) -> a + "." + b);
            if (refPackage == currentPackage || refPackageStr.equals("java.lang")) {
                return ref.getName();
            }
            else {
                return refPackageStr + "." + ref.getName();
            }
        }
    }

    public void writeSource(String outputDir, JavaClass javaClass) {
        final JavaPackage javaPackage = javaClass.getPackage();
        final ImmutableList<String> packageStringList = javaPackage.toStringList();
        File directory = new File(outputDir);
        if (directory.exists()) {
            for (String subfolder : packageStringList) {
                directory = new File(directory, subfolder);
                if (!directory.exists() && !directory.mkdir()) {
                    throw new RuntimeException("Unable to create directory " + directory);
                }
            }
        }

        final String path = outputDir + File.separator + packageStringList.map(s -> s + File.separator).reduce((a, b) -> a + b, "") + javaClass.getName() + ".java";
        try (FileOutputStream outStream = new FileOutputStream(path)) {
            final PrintWriter out = new PrintWriter(outStream, true);
            out.println("package " + packageStringList.reduce((a, b) -> a + "." + b) + ";");
            out.println();
            final StringBuilder classSignature = new StringBuilder();
            if (javaClass.isPublic()) {
                classSignature.append("public ");
            }

            if (javaClass.isFinal()) {
                classSignature.append("final ");
            }

            out.println(classSignature.append("class ").append(javaClass.getName()).append(" {"));
            final Formatter classFormatter = new JavaDefaultFormatter().increaseIndentation();
            for (JavaField field : javaClass.getFields()) {
                final StringBuilder fieldSignature = new StringBuilder(classFormatter.getIndentation());
                if (field.isPublic()) {
                    fieldSignature.append("public ");
                }
                else if (field.isProtected()) {
                    fieldSignature.append("protected ");
                }
                else if (field.isPrivate()) {
                    fieldSignature.append("private ");
                }

                if (field.isStatic()) {
                    fieldSignature.append("static ");
                }

                if (field.isFinal()) {
                    fieldSignature.append("final ");
                }

                out.println(fieldSignature
                        .append(typeToString(javaPackage, field.getType()))
                        .append(" ")
                        .append(field.getName())
                        .append(";"));
            }

            // TODO: Missing constructors

            for (JavaMethod method : javaClass.getMethods()) {
                final StringBuilder methodSignature = new StringBuilder(classFormatter.getIndentation());
                if (method.isPublic()) {
                    methodSignature.append("public ");
                }
                else if (method.isProtected()) {
                    methodSignature.append("protected ");
                }
                else if (method.isPrivate()) {
                    methodSignature.append("private ");
                }

                if (method.isStatic()) {
                    methodSignature.append("static ");
                }

                if (method.isFinal()) {
                    methodSignature.append("final ");
                }

                out.println();
                out.println(methodSignature
                        .append(typeToString(javaPackage, method.getReturnType()))
                        .append(" ")
                        .append(method.getName())
                        .append("(")
                        .append(method.getParameters()
                                .map(param -> typeToString(javaPackage, param.getType()) + " " + param.getName())
                                .reduce((a, b) -> a + ", " + b, ""))
                        .append(") {"));

                final Formatter methodFormatter = classFormatter.increaseIndentation();
                for (JavaStatement statement : method.getStatements()) {
                    out.println(statement.getText(methodFormatter));
                }

                out.println(classFormatter.getIndentation() + "}");
            }

            out.println("}");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
