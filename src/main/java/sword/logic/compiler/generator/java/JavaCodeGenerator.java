package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.logic.expressions.Expression;
import sword.logic.statements.Statement;
import sword.logic.types.EnumType;
import sword.logic.types.Type;

public final class JavaCodeGenerator {
    public JavaCodeGenerator(ImmutableMap<Expression, Type> expressionTypeMap) {

    }

    public Result generate(ImmutableList<Statement> statements) {
        return new Result();
    }

    public static final class Result {
        public ImmutableMap<Object, EnumType.Definition> getSourceEnums() {
            return null;
        }

        public ImmutableList<Object> getSourceStatements() {
            return null;
        }
    }
}