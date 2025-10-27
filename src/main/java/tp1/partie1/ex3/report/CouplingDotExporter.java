package tp1.partie1.ex3.report;

import tp1.partie1.ex3.model.CouplingEdge;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public class CouplingDotExporter {

    /**
     * Exporte un graphe non orienté (graph) pondéré.
     * - label = poids (et count entre parenthèses)
     * - penwidth mis à l'échelle du poids
     */
    public void exportUndirected(List<CouplingEdge> edges, String outPath) throws Exception {
        StringBuilder sb = new StringBuilder("graph Coupling {\n");
        sb.append("  layout=dot;\n  overlap=false;\n  splines=true;\n");
        sb.append("  node [shape=box, fontsize=11];\n");

        for (CouplingEdge e : edges) {
            double w = e.weight();
            double pen = 1.0 + 6.0 * w; // stylisation
            String label = String.format(Locale.US, "%.3f (%d)", w, e.count());
            sb.append("  \"").append(e.classA()).append("\" -- \"").append(e.classB()).append("\" ")
              .append("[label=\"").append(label)
              .append("\", penwidth=").append(String.format(Locale.US, "%.2f", pen))
              .append(", color=\"").append(w >= 0.1 ? "green" : (w >= 0.03 ? "orange" : "gray50"))
              .append("\"];\n");
        }
        sb.append("}\n");

        File out = new File(outPath);
        out.getParentFile().mkdirs();
        Files.writeString(out.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}
