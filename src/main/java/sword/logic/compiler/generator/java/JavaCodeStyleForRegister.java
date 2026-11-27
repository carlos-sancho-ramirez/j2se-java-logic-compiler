package sword.logic.compiler.generator.java;

public interface JavaCodeStyleForRegister {
    String getterName(String originalPropertyName, boolean isBooleanProperty);
    String propertyName(String originalPropertyName);
}
