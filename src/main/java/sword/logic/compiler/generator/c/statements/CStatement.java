package sword.logic.compiler.generator.c.statements;

import sword.logic.compiler.generator.Formatter;

public interface CStatement {
    String getText(Formatter formatter);
}
