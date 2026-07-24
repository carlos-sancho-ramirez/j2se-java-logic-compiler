package sword.logic.compiler;

public interface VariableNameCreator {
    /**
     * This method will create new variable names each time it is called.
     * <p>
     * This is here to avoid defining names in the same scope where another
     * variable with the same name already exists.
     *
     * @param suggestedName Suitable name.
     * @return The suitable name if it is not in use already, or any alternative if it is already in use.
     */
    String create(String suggestedName);
}
