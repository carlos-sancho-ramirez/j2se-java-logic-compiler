package sword.logic.compiler.generator.java;

/**
 * Code style for registers that follow the JavaBeans standard
 */
public final class JavaBeansCodeStyleForRegister implements JavaCodeStyleForRegister {
    @Override
    public String getterName(String originalPropertyName) {
        return "get" + (originalPropertyName.charAt(0) - 0x20) + originalPropertyName.substring(1);
    }

    @Override
    public String propertyName(String originalPropertyName) {
        return originalPropertyName;
    }
}
