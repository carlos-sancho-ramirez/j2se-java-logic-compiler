package sword.logic.compiler.generator.c;

public interface Formatter {
    String getIndentation();
    Formatter increaseIndentation();
}
