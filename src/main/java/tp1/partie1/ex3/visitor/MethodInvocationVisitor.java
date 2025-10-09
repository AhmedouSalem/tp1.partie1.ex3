package tp1.partie1.ex3.visitor;

import org.eclipse.jdt.core.dom.*;
import tp1.partie2.ex1.report.Reporter;

import java.util.*;

public class MethodInvocationVisitor extends ASTVisitor {
    private final CompilationUnit cu;
    private final String enclosingQualifiedName;
    private final Map<String, String> locals;
    private final Set<String> seenPerMethod = new HashSet<>();
    private final Reporter reporter;

    public MethodInvocationVisitor(CompilationUnit cu, String enclosingQualifiedName,
                             Map<String, String> locals, Reporter reporter) {
        this.cu = cu;
        this.enclosingQualifiedName = enclosingQualifiedName;
        this.locals = locals;
        this.reporter = reporter;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        String called = node.getName().getIdentifier();
        String receiver = inferReceiverType(node);

        IMethodBinding mb = node.resolveMethodBinding();
        if (mb != null && Modifier.isStatic(mb.getModifiers())) {
            ITypeBinding dc = mb.getDeclaringClass();
            if (dc != null) receiver = dc.getQualifiedName() + " (static)";
        }
        reporter.line("    → Appel : " + called + "    (receveur : " + simpleType(receiver) + ")");
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        String called = node.getName().getIdentifier();
        String receiver = "super(" + enclosingQualifiedName + ")";
        IMethodBinding mb = node.resolveMethodBinding();
        if (mb != null && mb.getDeclaringClass() != null)
            receiver = mb.getDeclaringClass().getQualifiedName();

        String signature = "super::" + called + "@" + node.getStartPosition();
        if (seenPerMethod.add(signature)) {
            reporter.line("    → Appel : " + called + "    (receveur : " + simpleType(receiver) + ")");
        }
        return true;
    }

    // --- heuristique inchangée ---
    private String inferReceiverType(MethodInvocation node) {
        Expression exp = node.getExpression();
        if (exp == null) return enclosingQualifiedName;

        ITypeBinding b = exp.resolveTypeBinding();
        if (b != null) return b.getQualifiedName();

        if (exp instanceof ThisExpression) return enclosingQualifiedName;
        if (exp instanceof Name) {
            String id = ((Name) exp).getFullyQualifiedName();
            if (locals.containsKey(id)) return locals.get(id);
            if (exp instanceof QualifiedName) {
                String last = ((QualifiedName) exp).getName().getIdentifier();
                if (locals.containsKey(last)) return locals.get(last);
            }
            return id;
        }
        if (exp instanceof FieldAccess) {
            String id = ((FieldAccess) exp).getName().getIdentifier();
            return locals.getOrDefault(id, id);
        }
        if (exp instanceof ClassInstanceCreation)
            return ((ClassInstanceCreation) exp).getType().toString();
        if (exp instanceof MethodInvocation)
            return "résultat(" + ((MethodInvocation) exp).getName().getIdentifier() + "())";
        if (exp instanceof QualifiedName) {
            QualifiedName qn = (QualifiedName) exp;
            String last = qn.getName().getIdentifier();
            if (locals.containsKey(last)) return locals.get(last);
            return qn.getFullyQualifiedName();
        }
        return exp.toString();
    }

    private static String simpleType(String qname) {
        if (qname == null) return "inconnu";
        int idx = qname.lastIndexOf('.');
        return idx >= 0 ? qname.substring(idx + 1) : qname;
    }
}
