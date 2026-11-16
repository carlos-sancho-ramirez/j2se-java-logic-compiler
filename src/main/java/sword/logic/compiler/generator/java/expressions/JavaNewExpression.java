package sword.logic.compiler.generator.java.expressions;

public final class JavaNewExpression implements JavaExpression {
    // TODO: Fix this once we have constructors
    @Override
    public String getText() {
        return "new MyClass()";
    }
}
