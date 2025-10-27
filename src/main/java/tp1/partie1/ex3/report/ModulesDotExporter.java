package tp1.partie1.ex3.report;

import tp1.partie1.ex3.service.ModuleExtractor.Module;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/** Graphe non orienté colorant chaque module avec une couleur dédiée. */
public class ModulesDotExporter {

    public void export(List<Module> modules, String outPath) throws Exception {
        StringBuilder sb = new StringBuilder("graph Modules {\n");
        sb.append("  layout=dot;\n  overlap=false;\n  splines=true;\n");
        sb.append("  node [shape=box, fontsize=11, style=filled];\n");

        // palette pastel
        List<String> colors = Arrays.asList(
                "#FFEEAD","#AED9E0","#C3F0CA","#F6C5C5","#D4C5F9","#FFE0AC",
                "#B8E1FF","#D9F7BE","#FFD6E7","#E6E6FA"
        );

        // émettre les nœuds par module
        for (int k=0; k<modules.size(); k++) {
            String color = colors.get(k % colors.size());
            sb.append("  subgraph cluster_").append(k+1).append(" {\n")
              .append("    label=\"Module ").append(k+1).append(" (avg=")
              .append(String.format(Locale.US,"%.3f", modules.get(k).avgSimilarity)).append(")\";\n")
              .append("    color=\"").append(color).append("\"; style=dashed;\n");
            for (String cls : modules.get(k).classes) {
                sb.append("    \"").append(cls).append("\" [fillcolor=\"").append(color).append("\"];\n");
            }
            sb.append("  }\n");
        }


        sb.append("}\n");
        File f = new File(outPath);
        f.getParentFile().mkdirs();
        Files.writeString(f.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}
