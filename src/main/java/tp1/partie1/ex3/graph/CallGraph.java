package tp1.partie1.ex3.graph;

import java.util.*;

public class CallGraph {
    public static final class Edge {
        public final String from;
        public final String to;
        public Edge(String from, String to) { this.from = from; this.to = to; }
    }

    private final Set<String> nodes = new LinkedHashSet<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(String id) { nodes.add(id); }
    public void addEdge(String from, String to) { edges.add(new Edge(from, to)); addNode(from); addNode(to); }

    public Set<String> nodes() { return nodes; }
    public List<Edge> edges() { return edges; }
}
