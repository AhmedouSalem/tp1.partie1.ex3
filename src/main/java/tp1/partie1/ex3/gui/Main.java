package tp1.partie1.ex3.gui;

import java.util.List;

import tp1.partie1.ex3.model.ClassInfo;
import tp1.partie1.ex3.model.MethodCall;
import tp1.partie1.ex3.model.MethodInfo;
import tp1.partie1.ex3.service.ClassAnalysisService;
import tp1.partie1.ex3.util.SourceScanner;

public class Main {
    public static final String projectSourcePath =
            "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";

    public static void main(String[] args) {
        String srcPath = (args != null && args.length > 0) ? args[0] : projectSourcePath;

        List<SourceScanner.SourceFile> files = SourceScanner.readJavaSourcesWithUnitNames(srcPath);
        if (files.isEmpty()) {
            System.err.println("Aucun fichier .java trouvé sous : " + srcPath);
            return;
        }

        ClassAnalysisService service = new ClassAnalysisService();

        List<ClassInfo> classes = service.analyze(srcPath, files);

        for (ClassInfo c : classes) {
            System.out.println("Classe : " + c.getQualifiedName());
            for (MethodInfo m : c.getMethods()) {
                System.out.println("  Méthode : " + m.getName());
                if (m.getCalls().isEmpty()) {
                    System.out.println("    (aucun appel)");
                } else {
                    for (MethodCall call : m.getCalls()) {
                        System.out.println("    Appelle : " + call.getCalleeName()
                                + "   [Type receveur : " + call.getReceiverType() + "]");
                    }
                }
            }
            System.out.println();
        }
    }
}
