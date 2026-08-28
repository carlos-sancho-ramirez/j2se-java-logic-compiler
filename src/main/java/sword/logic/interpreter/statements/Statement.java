package sword.logic.interpreter.statements;

import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.interpreter.Token;
import sword.logic.types.Type;

public interface Statement extends Finder {
    Token getName();

    /**
     * Returns the equivalent statement in the untokenized tree.
     * @param typeAliasResolverMap Map holding the typeAliasResolver that should be used for each expression, if required.
     * @param resolvedExpressions Map for all expressions already resolved.
     *                            This will be used in some expressions to determine which expression should be created.
     * @return The equivalent statement in the untokenized tree.
     */
    sword.logic.statements.Statement untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap);
}
