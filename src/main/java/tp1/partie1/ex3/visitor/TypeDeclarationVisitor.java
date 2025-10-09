package tp1.partie1.ex3.visitor;

import org.eclipse.jdt.core.dom.*;

import tp1.partie1.ex3.model.CallEdge;
import tp1.partie1.ex3.model.MethodRef;
import tp1.partie1.ex3.report.Reporter;

import java.util.*;
import java.util.function.Consumer;

public class TypeDeclarationVisitor extends ASTVisitor {
	private final CompilationUnit cu;
	private final Reporter reporter;
	private final Consumer<CallEdge> sink; // NEW
	private final Consumer<String> typeSink; // NEW

	public TypeDeclarationVisitor(CompilationUnit cu, Reporter reporter, Consumer<CallEdge> sink,
			Consumer<String> typeSink) {
		this.cu = cu;
		this.reporter = reporter;
		this.sink = sink;
		this.typeSink = typeSink; // NEW
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		ITypeBinding tb = node.resolveBinding();
		String classQualifiedName = tb != null ? tb.getQualifiedName() : node.getName().getIdentifier();
		if (typeSink != null) typeSink.accept(classQualifiedName); // NEW
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
				var svd = (SingleVariableDeclaration) p;
				localTypes.put(svd.getName().getIdentifier(), svd.getType().toString());
			}
			m.accept(new ASTVisitor() {
				@Override
				public boolean visit(VariableDeclarationStatement vds) {
					String t = vds.getType().toString();
					for (Object o : vds.fragments()) {
						var fr = (VariableDeclarationFragment) o;
						localTypes.put(fr.getName().getIdentifier(), t);
					}
					return super.visit(vds);
				}
			});

			// NEW: passe le "from" courant et le sink au MethodCallVisitor
			MethodRef from = new MethodRef(classQualifiedName, methodName);
			m.accept(new MethodInvocationVisitor(cu, classQualifiedName, localTypes, reporter, from, sink));
		}
		return false;
	}
}
