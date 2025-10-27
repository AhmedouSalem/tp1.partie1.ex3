package tp1.partie1.ex3.report;

import tp1.partie1.ex3.model.Dendrogram;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class DendrogramDotExporter {
    public void export(Dendrogram root, String outPath) throws Exception {
        StringBuilder sb = new StringBuilder("digraph Dendro {\n");
        sb.append("rankdir=TB;\nnode [shape=box, fontsize=10];\n");
        AtomicInteger id = new AtomicInteger(0);
        render(root, sb, id);
        sb.append("}\n");
        File out = new File(outPath); out.getParentFile().mkdirs();
        Files.writeString(out.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
    private int render(Dendrogram n, StringBuilder sb, AtomicInteger id) {
        int me = id.getAndIncrement();
        String label = n.isLeaf() ? n.classes.iterator().next() : String.format("h=%.3f\\n%s", n.height, n.classes);
        sb.append("n").append(me).append(" [label=\"").append(label).append("\"];\n");
        if (n.left!=null) { int l = render(n.left, sb, id); sb.append("n").append(me).append(" -> n").append(l).append(";\n"); }
        if (n.right!=null){ int r = render(n.right,sb, id); sb.append("n").append(me).append(" -> n").append(r).append(";\n"); }
        return me;
    }
}
