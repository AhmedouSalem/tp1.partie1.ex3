package tp1.partie1.ex3.visitor;

import org.eclipse.jdt.core.dom.*;

import tp1.partie1.ex3.model.MethodCall;
import tp1.partie1.ex3.model.MethodInfo;

public class MethodCallVisitor extends ASTVisitor {
    private final MethodInfo sink;
    private final ITypeBinding enclosingTypeBinding;

    public MethodCallVisitor(MethodInfo sink, ITypeBinding enclosingTypeBinding) {
        this.sink = sink;
        this.enclosingTypeBinding = enclosingTypeBinding;
    }

    private static String qName(ITypeBinding b) {
        return (b == null) ? "<?>"
                : (b.isAnonymous() ? b.getBinaryName() : b.getQualifiedName());
    }

    @Override
    public boolean visit(MethodInvocation node) {
        String callee = node.getName().getIdentifier();
        ITypeBinding exprTb = node.getExpression() != null
                ? node.getExpression().resolveTypeBinding()
                : null;

        String recv;
        if (exprTb != null) {
            recv = qName(exprTb);
        } else {
            IMethodBinding mb = node.resolveMethodBinding();
            recv = (mb != null && mb.getDeclaringClass() != null)
                    ? qName(mb.getDeclaringClass())
                    : qName(enclosingTypeBinding);
        }
        sink.addCall(new MethodCall(callee, recv));
        return true;
    }


    @Override
    public boolean visit(SuperMethodInvocation node) {
        String callee = node.getName().getIdentifier();
        IMethodBinding mb = node.resolveMethodBinding();
        String recv = (mb != null && mb.getDeclaringClass() != null)
                ? qName(mb.getDeclaringClass())
                : qName(enclosingTypeBinding);
        sink.addCall(new MethodCall(callee, recv));
        return super.visit(node);
    }

}