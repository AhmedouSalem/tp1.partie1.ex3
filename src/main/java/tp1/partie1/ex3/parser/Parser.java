package tp1.partie1.ex3.parser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import java.io.File;
import java.util.Map;

/**
 * Fabrique d'ASTParser avec bindings activés.
 * Elle tente d'auto-détecter la lib JRE :
 *  - Java 8:   $JAVA_HOME/jre/lib/rt.jar (ou $JAVA_HOME/lib/rt.jar)
 *  - Java 9+:  $JAVA_HOME/jmods (tous les .jmod passés en classpath)
 */
public class Parser {

    public static ASTParser newParserWithEnv(String projectSourcePath) {
        ASTParser parser = ASTParser.newParser(AST.JLS17); // OK 11+
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        // encodage + options
        @SuppressWarnings("unchecked")
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        // classpath
//        String javaHome = System.getProperty("java.home"); // JRE in use
        String[] classpath = new String[0];
        String[] sources = new String[] { projectSourcePath };
        String[] encodings = new String[] { "UTF-8" };

        parser.setEnvironment(classpath, sources, encodings, /*includeRunningVMBootclasspath*/ true);
        parser.setUnitName("Dummy.java"); // requis quand on parse via setSource(char[])
        return parser;
    }

//    private static String[] detectJreClasspath(String javaHome) {
//        File rt1 = new File(javaHome, "lib/rt.jar");
//        File rt2 = new File(new File(javaHome).getParentFile(), "lib/rt.jar");
//        if (rt1.exists()) return new String[]{ rt1.getAbsolutePath() };
//        if (rt2.exists()) return new String[]{ rt2.getAbsolutePath() };
//
//        // Java 9+ : jmods
//        File jmods = new File(javaHome, "jmods");
//        if (jmods.isDirectory()) {
//            File[] mods = jmods.listFiles((dir, name) -> name.endsWith(".jmod"));
//            if (mods != null && mods.length > 0) {
//                String[] cp = new String[mods.length];
//                for (int i = 0; i < mods.length; i++) cp[i] = mods[i].getAbsolutePath();
//                return cp;
//            }
//        }
//        // Fallback minimal : aucun (les bindings peuvent être partiels)
//        return new String[0];
//    }
}
