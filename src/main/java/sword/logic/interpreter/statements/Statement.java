package sword.logic.interpreter.statements;

import sword.logic.interpreter.Finder;
import sword.logic.syntax_tree.Token;

public interface Statement extends Finder {
    Token getName();
}
