package tp1.partie2.ex1.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import java.util.Map;

public class CompilationUnitFactory {
    private final String sourcePath;

    public CompilationUnitFactory(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public CompilationUnit parse(String source, String unitName) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setUnitName(unitName);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        // même configuration qu’avant
        parser.setEnvironment(null, new String[]{sourcePath}, null, true);

        return (CompilationUnit) parser.createAST(null);
    }
}
