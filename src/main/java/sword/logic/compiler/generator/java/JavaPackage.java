package sword.logic.compiler.generator.java;

import sword.collections.ImmutableList;

import java.util.Objects;

import static sword.logic.compiler.PreconditionUtils.ensureValidArguments;

public final class JavaPackage {
    public static boolean isValidPackageSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return false;
        }
        else {
            final int length = segment.length();
            char ch = segment.charAt(0);
            if (ch < 'a' || ch > 'z') {
                return false;
            }
            else {
                for (int i = 1; i < length; i++) {
                    ch = segment.charAt(i);
                    if ((ch < 'a' || ch > 'z') && (ch < '0' || ch > '9') && ch != '_') {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static JavaPackage createPackage(String segment) {
        return new JavaPackage(null, segment);
    }

    private final JavaPackage mParent;
    private final String mSegment;

    private JavaPackage(JavaPackage parent, String segment) {
        ensureValidArguments(isValidPackageSegment(segment));
        mParent = parent;
        mSegment = segment;
    }

    public ImmutableList<String> toStringList() {
        final ImmutableList<String> parentList = (mParent == null)?
                ImmutableList.empty() : mParent.toStringList();
        return parentList.append(mSegment);
    }

    public JavaPackage newSubpackage(String segment) {
        return new JavaPackage(this, segment);
    }

    public JavaClassReference newClassReference(String className) {
        return new JavaClassReference(this, className);
    }

    @Override
    public int hashCode() {
        return mSegment.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof JavaPackage that &&
                Objects.equals(mParent, that.mParent) &&
                mSegment.equals(that.mSegment);
    }
}
