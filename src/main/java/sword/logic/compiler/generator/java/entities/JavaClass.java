package sword.logic.compiler.generator.java.entities;

public interface JavaClass {
    JavaClassReference getReference();
    JavaPackage getPackage();
    String getName();
}
