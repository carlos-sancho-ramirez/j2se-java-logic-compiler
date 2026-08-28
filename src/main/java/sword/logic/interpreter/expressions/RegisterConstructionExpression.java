package sword.logic.interpreter.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableList;
import sword.collections.ImmutableListExtensions;
import sword.collections.ImmutableMap;
import sword.collections.ImmutableSet;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.SemanticErrorException;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.interpreter.UnresolvedTypeReferenceException;
import sword.logic.interpreter.scopes.Scope;
import sword.logic.interpreter.statements.ConstantDefinitionStatement;
import sword.logic.interpreter.statements.Statement;
import sword.logic.syntax_tree.Token;
import sword.logic.types.FunctionParameter;
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
    public void findAllExpressions(MutableMap<Expression, Scope> outMap, Scope scope) {
        outMap.put(this, scope);
        final Scope newScope = scope.createSubscopeWithStatements(mStatements);
        for (Statement statement : mStatements) {
            statement.findAllExpressions(outMap, newScope);
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
                return new RegisterType(regType.getFields().map(param -> {
                    for (Statement statement : mStatements) {
                        if (statement instanceof ConstantDefinitionStatement constDef && constDef.getName().getText().equals(param.getName())) {
                            final Type fieldType = resolvedExpressions.get(constDef.getExpression());
                            // TODO: Check that this field type fits inside the param provided type
                            return new FunctionParameter(param.getName(), fieldType);
                        }
                    }

                    return param;
                }).toSet());
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
}
