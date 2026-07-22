package sword.logic.syntax_tree.expressions;

import sword.collections.ImmutableList;
import sword.collections.ImmutableMap;
import sword.collections.Map;
import sword.collections.MutableMap;
import sword.collections.Procedure;
import sword.logic.compiler.UnresolvedReferenceException;
import sword.logic.syntax_tree.statements.ConstantDefinitionStatement;
import sword.logic.syntax_tree.statements.Statement;
import sword.logic.compiler.TypeMismatchException;
import sword.logic.syntax_tree.Token;
import sword.logic.syntax_tree.types.RegisterType;
import sword.logic.syntax_tree.types.Type;
import sword.logic.syntax_tree.types.UnknownType;

import static sword.logic.compiler.PreconditionUtils.ensureNonNull;
import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class RegisterConstructor implements Expression {
    private final Type mRequiredType;
    private final Token mType;
    private final ImmutableList<Statement> mStatements;

    public RegisterConstructor(Type requiredType, Token type, ImmutableList<Statement> statements) {
        ensureNonNull(requiredType, type, statements);
        ensureValidArguments(requiredType instanceof RegisterType || requiredType == UnknownType.getInstance());
        mRequiredType = requiredType;
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
    public Type requiredType() {
        return mRequiredType;
    }

    @Override
    public Expression requiresType(Type type) throws TypeMismatchException {
        if (type == UnknownType.getInstance() || type instanceof RegisterType regType && mRequiredType instanceof RegisterType resType && regType.getFields().equals(resType.getFields())) {
            return this;
        }
        else if (mRequiredType == UnknownType.getInstance() && type instanceof RegisterType regType) {
            return new RegisterConstructor(regType, mType, mStatements);
        }
        else {
            throw new TypeMismatchException("Unable to resolve this expression to " + type.getClass().getSimpleName() + ".");
        }
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
    }

    @Override
    public Type resultingType(Map<String, Type> paramTypes, Procedure<WarningMessage> logger) {
        ImmutableMap<Token, Type> fieldTypes = ((RegisterType) mRequiredType).getFields();
        final int fieldCount = fieldTypes.size();

        outer:
        for (Statement statement : mStatements) {
            if (statement instanceof ConstantDefinitionStatement constDef) {
                final String defText = constDef.getName().getText();
                for (int i = 0; i < fieldCount; i++) {
                    final Token key = fieldTypes.keyAt(i);
                    if (key.getText().equals(defText)) {
                        fieldTypes = fieldTypes.put(key, constDef.getExpression().resultingType(paramTypes, logger));
                        continue outer;
                    }
                }
            }
        }

        return new RegisterType(fieldTypes);
    }
}
