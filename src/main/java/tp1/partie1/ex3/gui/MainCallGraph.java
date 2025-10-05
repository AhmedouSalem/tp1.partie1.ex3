package tp1.partie1.ex3.gui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import tp1.partie1.ex3.graph.CallGraph;
import tp1.partie1.ex3.graph.CallGraphBuilder;
import tp1.partie1.ex3.graph.DotExporter;
import tp1.partie1.ex3.model.ClassInfo;
import tp1.partie1.ex3.service.ClassAnalysisService;
import tp1.partie1.ex3.util.SourceScanner;
import tp1.partie1.ex3.util.SourceScanner.SourceFile;

public class MainCallGraph {

    public static final String projectSourcePath =
            "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";

    public static void main(String[] args) throws Exception {
        String srcPath = (args != null && args.length > 0) ? args[0] : projectSourcePath;

        List<SourceFile> files = SourceScanner.readJavaSourcesWithUnitNames(srcPath);
        if (files.isEmpty()) {
            System.err.println("Aucun fichier .java trouvé sous : " + srcPath);
            return;
        }

        ClassAnalysisService analyzer = new ClassAnalysisService();
        List<ClassInfo> classes = analyzer.analyze(srcPath, files);

        CallGraph g = new CallGraphBuilder().buildMethodLevel(classes);
        String dot = new DotExporter().toDot(g);

        Path out = Path.of("callgraph.dot");
        Files.writeString(out, dot);
        System.out.println("   Fichier DOT généré → " + out.toAbsolutePath());
        System.out.println("   Si Graphviz est installé :");
        System.out.println("   dot -Tpng callgraph.dot -o callgraph.png");
    }
}
