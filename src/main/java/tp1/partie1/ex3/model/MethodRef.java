package tp1.partie1.ex3.model;

public record MethodRef(String typeName, String methodName) {
    @Override public String toString() { return typeName + "::" + methodName; }
}
