package tp1.partie1.ex3.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private final String name;
    private final List<MethodCall> calls = new ArrayList<>();

    public MethodInfo(String name) { this.name = name; }

    public String getName() { return name; }
    public List<MethodCall> getCalls() { return calls; }

    public void addCall(MethodCall c) { calls.add(c); }
}
