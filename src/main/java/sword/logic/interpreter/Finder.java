package sword.logic.interpreter;

import sword.collections.MutableMap;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.scopes.Scope;

public interface Finder {
    void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope);
}
