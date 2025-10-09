package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.service.AnalysisService;
import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.ConsoleReporter;

import java.io.File;
import java.util.List;

public class Application {
    public static String SOURCE_PATH =
        "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";

    public static void main(String[] args) throws Exception {
        if (args.length > 0) SOURCE_PATH = args[0];

        ConsoleReporter reporter = new ConsoleReporter();
        SourceScanner scanner = new SourceScanner();
        CompilationUnitFactory cuFactory = new CompilationUnitFactory(SOURCE_PATH);

        List<File> files = scanner.listJavaFiles(new File(SOURCE_PATH));
        if (files.isEmpty()) {
            reporter.info("Aucun fichier .java trouvé dans " + SOURCE_PATH);
            return;
        }

        AnalysisService service = new AnalysisService(cuFactory, reporter);
        service.analyze(files);
    }
}
