package tp1.partie2.ex1.service;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tp1.partie2.ex1.parser.CompilationUnitFactory;
import tp1.partie2.ex1.report.Reporter;
import tp1.partie1.ex3.visitor.TypeDeclarationVisitor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AnalysisService {
    private final CompilationUnitFactory cuFactory;
    private final Reporter reporter;

    public AnalysisService(CompilationUnitFactory cuFactory, Reporter reporter) {
        this.cuFactory = cuFactory;
        this.reporter = reporter;
    }

    public void analyze(List<File> files) throws Exception {
        for (File f : files) {
            String src = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            CompilationUnit cu = cuFactory.parse(src, f.getName());
            cu.accept(new TypeDeclarationVisitor(cu, reporter));
        }
    }
}
