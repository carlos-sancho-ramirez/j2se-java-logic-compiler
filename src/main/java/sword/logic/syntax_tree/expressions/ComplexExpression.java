package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.statements.ConstantDefinitionStatement;
import sword.logic.syntax_tree.statements.Statement;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.types.Type;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class ComplexExpression implements Expression {
    private final ImmutableList<Statement> mStatements;
    private final Expression mExpression;

    public ComplexExpression(ImmutableList<Statement> statements, Expression expression) {
        ensureValidArguments(!statements.isEmpty());
        mStatements = statements;
        mExpression = expression;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public ImmutableList<Statement> getStatements() {
        return mStatements;
    }

    @Override
    public Type resultingType() {
        return mExpression.resultingType();
    }

    @Override
    public Expression resultTo(Type type) throws TypeMismatchException {
        final Expression newExpression = mExpression.resultTo(type);
        return (newExpression == mExpression)? this : new ComplexExpression(mStatements, newExpression);
    }

    @Override
    public void resolveReferences(Map<String, ReferenceTarget> knownTargets) throws UnresolvedReferenceException {
        final MutableMap<String, ReferenceTarget> newTargets = knownTargets.mutate();
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                newTargets.put(constDef.getName().getText(), constDef);
            }
        }

        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                constDef.getExpression().resolveReferences(newTargets);
            }
        }

        mExpression.resolveReferences(newTargets);
    }
}
