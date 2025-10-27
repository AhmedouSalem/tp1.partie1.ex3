package tp1.partie1.ex3.service;

import tp1.partie1.ex3.model.CouplingEdge;

import java.util.*;

/** Matrice de similarité S[A][B] = weight (∈ [0,1]). Manque d'arête => 0. */
public class CouplingMatrix {
    private final List<String> classes;           // index -> className
    private final Map<String,Integer> indexOf;    // className -> index
    private final double[][] sim;                 // symétrique, diag = 0

    public CouplingMatrix(Set<String> projectTypes, Collection<CouplingEdge> edges) {
        this.classes = new ArrayList<>(new TreeSet<>(projectTypes));
        this.indexOf = new HashMap<>();
        for (int i=0;i<classes.size();i++) indexOf.put(classes.get(i), i);
        this.sim = new double[classes.size()][classes.size()];
        for (CouplingEdge e : edges) {
            Integer ia = indexOf.get(e.classA());
            Integer ib = indexOf.get(e.classB());
            if (ia==null || ib==null || ia.equals(ib)) continue;
            sim[ia][ib] = Math.max(sim[ia][ib], e.weight()); // si doublons, on garde le max
            sim[ib][ia] = sim[ia][ib];
        }
    }
    public List<String> classes() { return classes; }
    public double similarity(String a, String b) {
        Integer ia = indexOf.get(a), ib = indexOf.get(b);
        if (ia==null || ib==null) return 0.0;
        return sim[ia][ib];
    }
    public double similarity(Set<String> A, Set<String> B) {
        // average-link : moyenne des similarités entre paires (a∈A, b∈B)
        if (A.isEmpty() || B.isEmpty()) return 0.0;
        double sum = 0.0; int n = 0;
        for (String a : A) for (String b : B) { sum += similarity(a,b); n++; }
        return (n==0)?0.0:sum/n;
    }
}
