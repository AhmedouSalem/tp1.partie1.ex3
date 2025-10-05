package tp1.partie1.ex3.graph;

import tp1.partie1.ex3.model.ClassInfo;
import tp1.partie1.ex3.model.MethodCall;
import tp1.partie1.ex3.model.MethodInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallGraphBuilder {

    private static String methodNodeId(String ownerQualifiedClass, String methodSig) {
        return ownerQualifiedClass + "#" + methodSig;
    }

    public CallGraph buildMethodLevel(List<ClassInfo> classes) {
        CallGraph g = new CallGraph();

        // classes internes
        Set<String> internalOwners = new HashSet<>();
        for (ClassInfo c : classes) internalOwners.add(c.getQualifiedName());

        // 1) créer un nœud pour chaque méthode interne
        for (ClassInfo c : classes) {
            String owner = c.getQualifiedName();
            for (MethodInfo m : c.getMethods()) {
                g.addNode(methodNodeId(owner, m.getName()));
            }
        }

        // 2) créer les arêtes vers les méthodes appelées (internes ou externes)
        for (ClassInfo c : classes) {
            String callerOwner = c.getQualifiedName();
            for (MethodInfo m : c.getMethods()) {
                String from = methodNodeId(callerOwner, m.getName());

                for (MethodCall call : m.getCalls()) {
                    String recv = call.getReceiverType();
                    if (recv == null || recv.equals("<?>")) {
                        recv = "<external>";
                    }
                    String to = methodNodeId(recv, call.getCalleeName() + "()");
                    g.addEdge(from, to);
                }
            }
        }
        return g;
    }
}
