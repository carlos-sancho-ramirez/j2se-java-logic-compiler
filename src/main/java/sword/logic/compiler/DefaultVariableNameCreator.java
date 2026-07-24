package sword.logic.compiler;

import sword.collections.MutableHashSet;
import sword.collections.MutableSet;

public final class DefaultVariableNameCreator implements VariableNameCreator {
    private final MutableSet<String> mUsed = MutableHashSet.empty();

    @Override
    public String create(String suggestedName) {
        if (mUsed.add(suggestedName)) {
            return suggestedName;
        }
        else {
            for (int i = 2; i < 1000; i++) {
                String alternative = suggestedName + i;
                if (mUsed.add(alternative)) {
                    return alternative;
                }
            }

            throw new UnsupportedOperationException();
        }
    }
}
