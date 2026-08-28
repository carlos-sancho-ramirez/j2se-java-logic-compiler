package sword.logic.expressions;

import sword.collections.ImmutableHashSet;
import sword.collections.ImmutableSet;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;
import static sword.logic.statements.ConstantDefinitionStatement.validConstantName;

public final class ReferenceExpression implements Expression {
    private final String mReference;

    public ReferenceExpression(String reference) {
        ensureValidArguments(validConstantName(reference));
        mReference = reference;
    }

    public String getReference() {
        return mReference;
    }

    @Override
    public ImmutableSet<String> dependencies() {
        return new ImmutableHashSet.Builder<String>()
                .add(mReference)
                .build();
    }
}
