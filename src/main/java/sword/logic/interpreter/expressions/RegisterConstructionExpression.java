package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashMap;
import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.Finder;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.statements.ConstantDefinitionStatement;
import sword.logic.interpreter.statements.Statement;
import sword.logic.interpreter.type.definitions.TypeAliasResolver;
import sword.logic.syntax_tree.Token;
import sword.logic.types.RegisterType;
import sword.logic.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterConstructionExpression implements Expression {
    private final Token mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructionExpression(Token type, ImmutableList<Statement> statements) {
        ensureNonNull(type);
        ensureValidArguments(!statements.isEmpty());
        ensureValidArguments(ImmutableListExtensions.noneRepeated(statements.map(s -> s.getName().getText())));
        mType = type;
        mStatements = statements;
    }

    public Token getType() {
        return mType;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    @Override
    public void findAllFinders(MutableMap<Finder, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        final Scope newScope = scope.createSubscopeWithStatements(mStatements);
        for (Statement statement : mStatements) {
            statement.findAllFinders(outMap, newScope);
        }
    }

    @Override
    public ImmutableSet<String> dependencies() {
        ImmutableSet<String> result = ImmutableHashSet.empty();
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.addAll(constDef.getExpression().dependencies());
            }
        }

        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                result = result.remove(constDef.getName().getText());
            }
        }

        return result;
    }

    @Override
    public Type resolveType(Scope scope, Map<Expression, Type> resolvedExpressions) throws UnresolvedTypeReferenceException, UnresolvedReferenceException, SemanticErrorException {
        if (mStatements.allMatch(st -> !(st instanceof ConstantDefinitionStatement constDef) || resolvedExpressions.containsKey(constDef.getExpression()))) {
            final Type type = scope.resolveTypeAlias(mType);
            if (type instanceof RegisterType regType) {
                final RegisterType.Definition regDefinition = regType.getDefinition();
                final ImmutableMap<String, Type> defFields = regDefinition.getFields();
                final ImmutableMap.Builder<String, Type> builder = new ImmutableHashMap.Builder<>();
                for (String fieldName : defFields.keySet()) {
                    for (Statement statement : mStatements) {
                        if (statement instanceof ConstantDefinitionStatement constDef && constDef.getName().getText().equals(fieldName)) {
                            final Type fieldType = resolvedExpressions.get(constDef.getExpression());
                            final Type defType = defFields.get(fieldName);
                            if (defType.canFit(fieldType)) {
                                if (!defType.equals(fieldType)) {
                                    builder.put(fieldName, fieldType);
                                }
                            }
                            else {
                                throw new SemanticErrorException("Unable to assign value for field '" + fieldName + "'. Type mismatch", constDef.getName().getLine(), constDef.getName().getColumn());
                            }
                        }
                    }
                }

                return new RegisterType(regDefinition, builder.build());
            }
            else {
                throw new SemanticErrorException("Expected register type", mType.getLine(), mType.getColumn());
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Type resolveType(ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public ImmutableMap<String, Type> restrictionMap(Type resultType, Map<Expression, Type> resolvedExpressions, ImmutableMap<String, Type> restrictionMap) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public sword.logic.expressions.RegisterConstructionExpression untokenize(ImmutableMap<Finder, ? extends TypeAliasResolver> typeAliasResolverMap, Map<Expression, Type> resolvedExpressions) {
        return new sword.logic.expressions.RegisterConstructionExpression(mType.getText(), mStatements.map(st -> st.untokenize(typeAliasResolverMap, resolvedExpressions)));
    }
}
