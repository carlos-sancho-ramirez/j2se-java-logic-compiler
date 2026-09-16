package sword.logic.compiler.generator.c.expressions;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class CReferenceExpression implements CAssignableExpression {
    private final String mReference;

    /**
     * Checks if a reference is valid or not.
     * <p>
     * In most of the cases, this follow the same rules described at {@link sword.logic.statements.ConstantDefinitionStatement#validConstantName(String)}.
     * So, they follow the camel-case format, using an uppercase in front of any new word instead of adding spaces.
     * However, some extra possibilities are allowed for resulting C code to avoid problems.
     * <ul>
     *     <li>It can start with underscore if it is a local variable that was not present in the original code.</li>
     *     <li>It can have underscores in the middle to isolate scope names. Note that functions defined inside scopes can collide with other with the same name in other scopes if the scope is not part of the function name.</li>
     * </ul>
     * In any case, note that:
     * <ul>
     *     <li>it is not allowed to have an underscore just after another underscore.</li>
     *     <li>reference names cannot end with underscore.</li>
     *     <li>Only lower-case characters from a to z are allowed after each underscore.</li>
     * </ul>
     *
     * @param reference Reference name to be evaluated
     * @return Whether the reference name is valid or not.
     */
    private static boolean isValidReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            return false;
        }
        else {
            char ch = reference.charAt(0);
            boolean lastIsUnderscore;
            if (ch >= 'a' && ch <= 'z') {
                lastIsUnderscore = false;
            }
            else if (ch == '_') {
                lastIsUnderscore = true;
            }
            else {
                return false;
            }

            final int length = reference.length();
            for (int i = 1; i < length; i++) {
                ch = reference.charAt(i);
                if (ch >= 'a' && ch <= 'z') {
                    lastIsUnderscore = false;
                }
                else if (ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                    if (lastIsUnderscore) {
                        return false;
                    }

                    lastIsUnderscore = false;
                }
                else if (ch == '_') {
                    if (lastIsUnderscore) {
                        return false;
                    }

                    lastIsUnderscore = true;
                }
                else {
                    return false;
                }
            }

            return !lastIsUnderscore;
        }
    }

    public CReferenceExpression(String reference) {
        ensureValidArguments(isValidReference(reference));
        mReference = reference;
    }

    @Override
    public String getText() {
        return mReference;
    }
}
