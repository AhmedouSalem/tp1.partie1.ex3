package tp1.partie1.ex3.graph;

public class DotExporter {
    public String toDot(CallGraph g) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CallGraph {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=box, fontsize=10];\n");

        for (String n : g.nodes()) {
            sb.append("  \"").append(n).append("\";\n");
        }
        for (CallGraph.Edge e : g.edges()) {
            sb.append("  \"").append(e.from).append("\" -> \"").append(e.to).append("\";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
