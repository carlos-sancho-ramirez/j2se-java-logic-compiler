package sword.logic.types;

public final class EmptyArrayType implements Type {
    private static EmptyArrayType instance;
    public static EmptyArrayType getInstance() {
        if (instance == null) {
            instance = new EmptyArrayType();
        }

        return instance;
    }

    private EmptyArrayType() {
    }

    public IntType getLengthType() {
        return IntType.getAlwaysZeroType();
    }

    @Override
    public Type getIntersection(Type other) {
        return (this == other || other instanceof ArrayType arrayType && getLengthType().canFit(arrayType.getLengthType()))? this : null;
    }

    @Override
    public Type getUnion(Type other) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public boolean canFit(Type newType) {
        return newType == this;
    }
}
