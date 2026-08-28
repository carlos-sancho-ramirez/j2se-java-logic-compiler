package sword.logic.interpreter.statements;

import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.expressions.Expression;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.interpreter.type.definitions.TypeDefinition;
import sword.logic.syntax_tree.Token;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.TypeDefinitionStatement.validTypeName;

public final class TypeDefinitionStatement implements Statement {
    private final Token mName;
    private final TypeDefinition mDefinition;

    public TypeDefinitionStatement(Token name, TypeDefinition definition) {
        ensureNonNull(name, definition);
        ensureValidArguments(validTypeName(name.getText()));

        mName = name;
        mDefinition = definition;
    }

    @Override
    public Token getName() {
        return mName;
    }

    @Override
    public sword.logic.statements.TypeDefinitionStatement untokenize(
            ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap,
            Map<Expression, Type> resolvedExpressions,
            MutableMap<Expression, sword.logic.expressions.Expression> outExpressionMap) {
        try {
            return new sword.logic.statements.TypeDefinitionStatement(mName.getText(), mDefinition.resolve(typeAliasResolverMap.get(this)));
        }
        catch (UnresolvedTypeReferenceException e) {
            throw new RuntimeException("Unable to untokenize type", e);
        }
    }

    public TypeDefinition getDefinition() {
        return mDefinition;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
    }
}
