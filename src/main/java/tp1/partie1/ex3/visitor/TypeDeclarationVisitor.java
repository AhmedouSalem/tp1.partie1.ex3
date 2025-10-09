package tp1.partie1.ex3.visitor;

import org.eclipse.jdt.core.dom.*;
import tp1.partie2.ex1.report.Reporter;

import java.util.*;

public class TypeDeclarationVisitor extends ASTVisitor {
    private final CompilationUnit cu;
    private final Reporter reporter;

    public TypeDeclarationVisitor(CompilationUnit cu, Reporter reporter) {
        this.cu = cu;
        this.reporter = reporter;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        ITypeBinding tb = node.resolveBinding();
        String classQualifiedName = tb != null ? tb.getQualifiedName()
                                               : node.getName().getIdentifier();
        String simple = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);
        reporter.line("Classe : " + simple);

        Map<String, String> fieldTypes = new HashMap<>();
        for (FieldDeclaration fd : node.getFields()) {
            String type = fd.getType().toString();
            for (Object f : fd.fragments()) {
                VariableDeclarationFragment v = (VariableDeclarationFragment) f;
                fieldTypes.put(v.getName().getIdentifier(), type);
            }
        }

        List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
        methods.sort(Comparator.comparing(m -> m.getName().getIdentifier()));

        for (MethodDeclaration m : methods) {
            String methodName = m.getName().getIdentifier();
            reporter.line("  Méthode : " + methodName);

            Map<String, String> localTypes = new HashMap<>(fieldTypes);
            for (Object p : m.parameters()) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
                localTypes.put(svd.getName().getIdentifier(), svd.getType().toString());
            }
            m.accept(new ASTVisitor() {
                @Override public boolean visit(VariableDeclarationStatement vds) {
                    String t = vds.getType().toString();
                    for (Object o : vds.fragments()) {
                        VariableDeclarationFragment fr = (VariableDeclarationFragment) o;
                        localTypes.put(fr.getName().getIdentifier(), t);
                    }
                    return super.visit(vds);
                }
            });

            m.accept(new MethodInvocationVisitor(cu, classQualifiedName, localTypes, reporter));
        }
        return false;
    }
}
