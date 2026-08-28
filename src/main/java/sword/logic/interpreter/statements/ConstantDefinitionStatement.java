package sword.logic.interpreter.statements;

import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.interpreter.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class ConstantDefinitionStatement implements Statement {
    private final Token mName;
    private final Expression mExpression;

    public ConstantDefinitionStatement(Token name, Expression expression) {
        ensureValidArguments(validConstantName(name.getText()));
        ensureNonNull(expression);
        mName = name;
        mExpression = expression;
    }

    @Override
    public Token getName() {
        return mName;
    }

    @Override
    public sword.logic.statements.ConstantDefinitionStatement untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap) {
        return new sword.logic.statements.ConstantDefinitionStatement(mName.getText(), mExpression.untokenize(typeAliasResolverMap, resolvedExpressions, outExpressionMap));
    }

    public Expression getExpression() {
        return mExpression;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        mExpression.findAllFinders(outMap, scope);
    }
}
