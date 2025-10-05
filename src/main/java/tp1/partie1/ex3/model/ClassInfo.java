package tp1.partie1.ex3.model;

import java.util.ArrayList;
import java.util.List;

public class ClassInfo {
    private final String qualifiedName;
    private final List<MethodInfo> methods = new ArrayList<>();

    public ClassInfo(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedName() { return qualifiedName; }
    public List<MethodInfo> getMethods() { return methods; }

    public void addMethod(MethodInfo m) { methods.add(m); }
}
