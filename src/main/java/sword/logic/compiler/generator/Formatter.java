package sword.logic.compiler.generator;

public interface Formatter {
    String getIndentation();
    Formatter increaseIndentation();
}
