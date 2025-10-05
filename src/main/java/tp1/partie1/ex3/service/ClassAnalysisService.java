package tp1.partie1.ex3.service;

import org.eclipse.jdt.core.dom.*;

import tp1.partie1.ex3.model.ClassInfo;
import tp1.partie1.ex3.model.MethodInfo;
import tp1.partie1.ex3.parser.Parser;
import tp1.partie1.ex3.util.SourceScanner;
import tp1.partie1.ex3.visitor.MethodCallVisitor;

import java.util.ArrayList;
import java.util.List;

public class ClassAnalysisService {

	// service/ClassAnalysisService.java (dans analyze)
	public List<ClassInfo> analyze(String projectSourcePath, List<SourceScanner.SourceFile> files) {
	    List<ClassInfo> results = new ArrayList<>();
	    ASTParser parser = Parser.newParserWithEnv(projectSourcePath);

	    for (SourceScanner.SourceFile sf : files) {
	        parser.setUnitName(sf.unitName);          // <-- CRUCIAL
	        parser.setSource(sf.content.toCharArray());
	        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

	        cu.accept(new ASTVisitor() {
	            @Override public boolean visit(TypeDeclaration type) {
	                if (type.isInterface()) return true;
	                ITypeBinding tb = type.resolveBinding();
	                String qn = (tb != null && tb.getQualifiedName() != null)
	                        ? tb.getQualifiedName() : type.getName().getIdentifier();

	                ClassInfo ci = new ClassInfo(qn);
	                for (MethodDeclaration m : type.getMethods()) {
	                    MethodInfo mi = new MethodInfo(buildMethodSig(m));
	                    Block body = m.getBody();
	                    if (body != null) {
	                        ITypeBinding enclosing = (tb != null) ? tb : null;
	                        body.accept(new MethodCallVisitor(mi, enclosing)); // ta version avec fallback mb
	                    }
	                    ci.addMethod(mi);
	                }
	                results.add(ci);
	                return true;
	            }
	        });
	    }
	    return results;
	}


    private static String buildMethodSig(MethodDeclaration m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName().getIdentifier()).append("(");
        if (m.parameters() != null) {
            for (int i = 0; i < m.parameters().size(); i++) {
                SingleVariableDeclaration p = (SingleVariableDeclaration) m.parameters().get(i);
                ITypeBinding tb = p.getType().resolveBinding();
                String t = (tb != null && tb.getQualifiedName() != null)
                        ? tb.getQualifiedName() : p.getType().toString();
                if (i > 0) sb.append(", ");
                sb.append(t);
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
