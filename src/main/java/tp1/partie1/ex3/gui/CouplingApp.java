package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.ConsoleReporter;
import tp1.partie1.ex3.report.CouplingDotExporter;
import tp1.partie1.ex3.service.AnalysisService;
import tp1.partie1.ex3.service.CouplingAnalyzer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CouplingApp {
    public static void main(String[] args) throws Exception {
        String SOURCE_PATH = (args.length > 0) ? args[0]
                : "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";

        var reporter = new ConsoleReporter();
        var scanner = new SourceScanner();
        var cuFactory = new CompilationUnitFactory(SOURCE_PATH);

        var files = scanner.listJavaFiles(new File(SOURCE_PATH));
        if (files.isEmpty()) {
            System.out.println("Aucun .java dans " + SOURCE_PATH);
            return;
        }

        // 2) Exécuter l'analyse d'appels (TP1)
        var analysis = new AnalysisService(cuFactory, reporter);
        analysis.analyze(files);

        // 3) Calcul du couplage (TP2)
        var coupling = new CouplingAnalyzer()
                .compute(analysis.getCallEdges(), analysis.getProjectTypes());

        // (optionnel) Export CSV
        var csv = new CouplingAnalyzer().toCsv(coupling);
        Files.writeString(new File("target/coupling.csv").toPath(), csv, StandardCharsets.UTF_8);

        // 4) Export graphe de couplage (DOT -> PNG)
        var exporter = new CouplingDotExporter();
        exporter.exportUndirected(coupling, "target/coupling.dot");

        // Génère l'image si 'dot' est installé
        try {
            new ProcessBuilder("dot", "-Tpng", "target/coupling.dot", "-o", "target/coupling.png")
                    .inheritIO().start().waitFor();
            System.out.println("OK: target/coupling.png");
        } catch (Exception ex) {
            System.out.println("Astuce: installe Graphviz pour produire le PNG: sudo apt-get install graphviz");
        }

        System.out.println("OK: target/coupling.dot, target/coupling.csv");
    }
}
