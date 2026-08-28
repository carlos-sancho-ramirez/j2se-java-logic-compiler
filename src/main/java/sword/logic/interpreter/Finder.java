package sword.logic.interpreter;

import sword.collections.MutableMap;
import sword.logic.interpreter.scopes.Scope;

public interface Finder {
    void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope);
}
