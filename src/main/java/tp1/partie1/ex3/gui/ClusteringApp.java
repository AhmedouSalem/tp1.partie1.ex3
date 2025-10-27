package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.*;
import tp1.partie1.ex3.service.*;
import java.io.File;

public class ClusteringApp {
    public static void main(String[] args) throws Exception {
        String SRC = (args.length>0)? args[0] : "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";
        double CP = (args.length>1)? Double.parseDouble(args[1]) : 0.10; // seuil par défaut

        var reporter = new ConsoleReporter();
        var files = new SourceScanner().listJavaFiles(new File(SRC));
        if (files.isEmpty()) { System.out.println("Aucun .java"); return; }

        var analysis = new AnalysisService(new CompilationUnitFactory(SRC), reporter);
        analysis.analyze(files);

        // couplage (TP2 ex1 déjà fait)
        var coupling = new CouplingAnalyzer().compute(analysis.getCallEdges(), analysis.getProjectTypes());
        new CouplingDotExporter().exportUndirected(coupling, "target/coupling.dot");
        try { new ProcessBuilder("dot","-Tpng","target/coupling.dot","-o","target/coupling.png").start().waitFor(); } catch(Exception ignored){}

        // matrice & clustering
        var matrix = new CouplingMatrix(analysis.getProjectTypes(), coupling);
        var dendro = new HierarchicalClustering().fit(matrix);
        new DendrogramDotExporter().export(dendro, "target/dendrogram.dot");
        try { new ProcessBuilder("dot","-Tpng","target/dendrogram.dot","-o","target/dendrogram.png").start().waitFor(); } catch(Exception ignored){}

        // extraction des modules sous contraintes
        var modules = new ModuleExtractor().extract(dendro, matrix, CP);
        new ModulesCsvExporter().export(modules, "target/modules.csv");

        System.out.println("OK: target/dendrogram.png, target/modules.csv (CP="+CP+")");
    }
}
