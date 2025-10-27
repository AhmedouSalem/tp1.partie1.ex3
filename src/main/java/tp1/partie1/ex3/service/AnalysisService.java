package tp1.partie1.ex3.service;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tp1.partie1.ex3.model.CallEdge;
import tp1.partie1.ex3.model.MethodRef;
import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.report.Reporter;
import tp1.partie1.ex3.visitor.TypeDeclarationVisitor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AnalysisService {
    private final CompilationUnitFactory cuFactory;
    private final Reporter reporter;

    // stockage du graphe
    private final Set<CallEdge> edges = new LinkedHashSet<>();
    private final Set<String> projectTypes = new LinkedHashSet<>();

    public AnalysisService(CompilationUnitFactory cuFactory, Reporter reporter) {
        this.cuFactory = cuFactory;
        this.reporter = reporter;
    }

    public void analyze(List<File> files) throws Exception {
        Consumer<CallEdge> edgeSink = edges::add; // collecteur d'arêtes
        Consumer<String> typeSink = projectTypes::add;

        for (File f : files) {
            String src = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            CompilationUnit cu = cuFactory.parse(src, f.getName());
            cu.accept(new TypeDeclarationVisitor(cu, reporter, edgeSink, typeSink));
        }

        // export du graphe
        exportDot("target/callgraph.dot");
        exportPlantUml("target/callgraph.puml");
        reporter.info("Graphe d'appel exporté : target/callgraph.dot et target/callgraph.puml");
    }

    private void exportDot(String path) throws Exception {
        StringBuilder sb = new StringBuilder("digraph Calls {\n");
        sb.append("  rankdir=LR;\n  node [shape=box, fontsize=11];\n");
        
     // émettre les nœuds avec style
        for (String t : projectTypes) {
            sb.append("  ").append("\"").append(t).append("\"")
              .append(" [style=filled, fillcolor=white];\n");
        }
        // regrouper les externes rencontrés
        Set<String> externals = edges.stream()
            .map(e -> e.to().typeName())
            .filter(t -> !projectTypes.contains(t))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        for (String t : externals) {
            sb.append("  ").append("\"").append(t).append("\"")
              .append(" [style=filled, fillcolor=lightgray];\n");
        }
        
        for (CallEdge e : edges) {
            boolean internal = projectTypes.contains(e.to().typeName());
            String style = internal ? "color=green, penwidth=1.6"
                                    : "color=gray50, style=dashed";
            sb.append("  ")
              .append(sanitize(e.from()))
              .append(" -> ")
              .append(sanitize(e.to()))
              .append(" [").append(style).append("]")
              .append(";\n");
        }
        sb.append("}\n");
        Files.createDirectories(new File("target").toPath());
        Files.writeString(new File(path).toPath(), sb.toString(), StandardCharsets.UTF_8);
    }


    private void exportPlantUml(String path) throws Exception {
        StringBuilder sb = new StringBuilder("@startuml\nleft to right direction\n");
        for (CallEdge e : edges) {
            boolean internal = projectTypes.contains(e.to().typeName());
            String color = internal ? "#green" : "#gray";
            sb.append("\"").append(e.from()).append("\" --> ").append(color)
              .append(" ").append("\"").append(e.to()).append("\"\n");
        }
        sb.append("@enduml\n");
        Files.writeString(new File(path).toPath(), sb.toString(), StandardCharsets.UTF_8);
    }


    private static String sanitize(MethodRef m) {
        // identifiants DOT sûrs
        return "\"" + m.typeName().replace('"','\'') + "::" + m.methodName().replace('"','\'') + "\"";
    }
    
    public java.util.Set<tp1.partie1.ex3.model.CallEdge> getCallEdges() { return java.util.Set.copyOf(edges); }
    public java.util.Set<String> getProjectTypes() { return java.util.Set.copyOf(projectTypes); }

}
