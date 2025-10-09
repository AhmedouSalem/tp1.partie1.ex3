package tp1.partie1.ex3.gui;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyse tout un dossier src et, pour chaque classe, liste:
 *  - ses méthodes
 *  - pour chaque méthode, les appels de méthodes (nom + type statique du receveur)
 *
 * Dépendances Maven:
 *   org.eclipse.jdt:org.eclipse.jdt.core
 *   commons-io:commons-io
 */
public class MethodCallExtractor {

    // ➜ Mets ici le chemin vers TON dossier src
    public static final String SOURCE_PATH =
            "/home/ahmedou-salem/eclipse-workspace/project.exemple.etude/src";

    public static void main(String[] args) throws Exception {
        List<File> files = listJavaFiles(new File(SOURCE_PATH));
        if (files.isEmpty()) {
            System.err.println("Aucun fichier .java trouvé dans " + SOURCE_PATH);
            return;
        }

        for (File f : files) {
            String src = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
            CompilationUnit cu = parse(src, f.getName());
            cu.accept(new ClassVisitor(cu));
        }
    }

    // ----------- Utils fichiers -----------
    private static List<File> listJavaFiles(File root) {
        if (!root.exists()) return Collections.emptyList();
        return FileUtils.listFiles(root, new String[]{"java"}, true)
                .stream().collect(Collectors.toList());
    }

    // ----------- Parser JDT -----------
    private static CompilationUnit parse(String source, String unitName) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setUnitName(unitName);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        // Environnement minimal: on donne uniquement le sourcepath.
        // (pour une résolution parfaite, fournir aussi le classpath JRE/JARs)
        parser.setEnvironment(null, new String[]{SOURCE_PATH}, null, /*includeRunningVMBootclasspath*/ true);

        return (CompilationUnit) parser.createAST(null);
    }

    // ===================== VISITEURS =====================
    static class ClassVisitor extends ASTVisitor {
        private final CompilationUnit cu;

        ClassVisitor(CompilationUnit cu) {
            this.cu = cu;
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            ITypeBinding tb = node.resolveBinding();
            String classQualifiedName = tb != null ? tb.getQualifiedName() : node.getName().getIdentifier();
            System.out.println("Classe : " + classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1));

            // carte (champ -> type)
            Map<String, String> fieldTypes = new HashMap<>();
            for (FieldDeclaration fd : node.getFields()) {
                String type = fd.getType().toString();
                for (Object f : fd.fragments()) {
                    VariableDeclarationFragment v = (VariableDeclarationFragment) f;
                    fieldTypes.put(v.getName().getIdentifier(), type);
                }
            }

            // trier les méthodes par nom (lisibilité)
            List<MethodDeclaration> methods = Arrays.asList(node.getMethods());
            methods.sort(Comparator.comparing(m -> m.getName().getIdentifier()));

            for (MethodDeclaration m : methods) {
                // si tu veux ignorer les constructeurs, dé-commente la ligne suivante:
                // if (m.isConstructor()) continue;

                String methodName = m.getName().getIdentifier();
                System.out.println("  Méthode : " + methodName);

                // table des types visibles dans la méthode
                Map<String, String> localTypes = new HashMap<>(fieldTypes);

                // paramètres
                for (Object p : m.parameters()) {
                    SingleVariableDeclaration svd = (SingleVariableDeclaration) p;
                    localTypes.put(svd.getName().getIdentifier(), svd.getType().toString());
                }

                // variables locales (collecte préalable pour l’heuristique)
                m.accept(new ASTVisitor() {
                    @Override
                    public boolean visit(VariableDeclarationStatement vds) {
                        String t = vds.getType().toString();
                        for (Object o : vds.fragments()) {
                            VariableDeclarationFragment fr = (VariableDeclarationFragment) o;
                            localTypes.put(fr.getName().getIdentifier(), t);
                        }
                        return super.visit(vds);
                    }
                });

                String enclosingQName = classQualifiedName;

                // extraction des appels
                m.accept(new MethodCallVisitor(cu, enclosingQName, localTypes));
            }
            return false; // évite d'explorer classes internes; passe à true si tu veux
        }
    }

    static class MethodCallVisitor extends ASTVisitor {
        private final CompilationUnit cu;
        private final String enclosingQualifiedName;
        private final Map<String, String> locals; // champs + params + locales
        private final Set<String> seenPerMethod = new HashSet<>();

        MethodCallVisitor(CompilationUnit cu, String enclosingQualifiedName, Map<String, String> locals) {
            this.cu = cu;
            this.enclosingQualifiedName = enclosingQualifiedName;
            this.locals = locals;
        }
        
        @Override
        public boolean visit(MethodInvocation node) {
            String called = node.getName().getIdentifier();

            // 1) type statique du receveur (prioritaire pour l'énoncé)
            String receiver = inferReceiverType(node); // exp.resolveTypeBinding() ou enclosing type si exp==null

            // 2) si la méthode est statique, on peut afficher la classe déclarante
            IMethodBinding mb = node.resolveMethodBinding();
            if (mb != null) {
                if (Modifier.isStatic(mb.getModifiers())) {
                    ITypeBinding dc = mb.getDeclaringClass();
                    if (dc != null) {
                        receiver = dc.getQualifiedName() + " (static)";
                    }
                }
            }

            System.out.println("    → Appel : " + called + "    (receveur : " + simpleType(receiver) + ")");
            return true;
        }


        @Override
        public boolean visit(SuperMethodInvocation node) {
            String called = node.getName().getIdentifier();
            String receiver = "super(" + enclosingQualifiedName + ")";
            IMethodBinding mb = node.resolveMethodBinding();
            if (mb != null && mb.getDeclaringClass() != null) {
                receiver = mb.getDeclaringClass().getQualifiedName();
            }
            String signature = "super::" + called + "@" + node.getStartPosition();
            if (seenPerMethod.add(signature)) {
                System.out.println("    → Appel : " + called + "    (receveur : " + simpleType(receiver) + ")");
            }
            return true;
        }

        // --------- heuristique si pas de bindings ---------
        private String inferReceiverType(MethodInvocation node) {
            Expression exp = node.getExpression();

            // appel implicite this.foo()
            if (exp == null) {
                return enclosingQualifiedName;
            }

            // bindings présents ?
            ITypeBinding b = exp.resolveTypeBinding();
            if (b != null) return b.getQualifiedName();

            if (exp instanceof ThisExpression) {
                return enclosingQualifiedName;
            }
            if (exp instanceof Name) {
                String id = ((Name) exp).getFullyQualifiedName();
                if (locals.containsKey(id)) return locals.get(id);
                if (exp instanceof QualifiedName) {
                    String last = ((QualifiedName) exp).getName().getIdentifier();
                    if (locals.containsKey(last)) return locals.get(last);
                }
                // potentiellement appel statique de TypeName.method()
                return id;
            }
            if (exp instanceof FieldAccess) {
                String id = ((FieldAccess) exp).getName().getIdentifier();
                return locals.getOrDefault(id, id);
            }
            if (exp instanceof ClassInstanceCreation) {
                return ((ClassInstanceCreation) exp).getType().toString();
            }
            if (exp instanceof MethodInvocation) {
                // chaîne d'appels : foo().bar()
                return "résultat(" + ((MethodInvocation) exp).getName().getIdentifier() + "())";
            }
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
}
