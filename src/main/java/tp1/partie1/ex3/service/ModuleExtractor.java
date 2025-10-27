package tp1.partie1.ex3.service;

import tp1.partie1.ex3.model.Dendrogram;

import java.util.*;

public class ModuleExtractor {

    public static class Module {
        public final Set<String> classes;
        public final double avgSimilarity; // moyenne des poids à l'intérieur du module
        public Module(Set<String> c, double avg) { this.classes = c; this.avgSimilarity = avg; }
        @Override public String toString(){ return classes + " avg=" + String.format(Locale.US,"%.3f",avgSimilarity); }
    }

    /** Retourne des modules disjoints (branches du dendrogramme) qui respectent les règles. */
    public List<Module> extract(Dendrogram root, CouplingMatrix M, double CP) {
        List<Module> modules = new ArrayList<>();
        splitRec(root, M, CP, modules);
        int Mclasses = M.classes().size();
        int maxModules = Math.max(1, Mclasses/2);

        // Si trop de modules, on fusionne les plus faibles
        while (modules.size() > maxModules) {
            int ia = -1, ib = -1; double bestGain = Double.NEGATIVE_INFINITY;
            for (int i=0;i<modules.size();i++) {
                for (int j=i+1;j<modules.size();j++) {
                    double avg = averageSimilarity(union(modules.get(i).classes, modules.get(j).classes), M);
                    double gain = avg - (modules.get(i).avgSimilarity + modules.get(j).avgSimilarity)/2.0;
                    if (gain > bestGain) { bestGain = gain; ia=i; ib=j; }
                }
            }
            Set<String> mergedSet = union(modules.get(ia).classes, modules.get(ib).classes);
            modules.remove(ib); modules.remove(ia);
            modules.add(new Module(mergedSet, averageSimilarity(mergedSet, M)));
        }
        // tri modules par score décroissant
        modules.sort(Comparator.comparingDouble((Module mo) -> mo.avgSimilarity).reversed());
        return modules;
    }

    private void splitRec(Dendrogram node, CouplingMatrix M, double CP, List<Module> out) {
        double avg = averageSimilarity(node.classes, M);
        if (avg >= CP || node.isLeaf()) {
            out.add(new Module(node.classes, avg));
            return;
        }
        // sinon, descendre si possible
        if (node.left != null) splitRec(node.left, M, CP, out);
        if (node.right!= null) splitRec(node.right, M, CP, out);
    }

    private static Set<String> union(Set<String> A, Set<String> B) {
        Set<String> u = new LinkedHashSet<>(A); u.addAll(B); return u;
    }

    /** moyenne des similarités sur toutes les paires (i<j) du set; 0 si |S|<2 */
    public static double averageSimilarity(Set<String> S, CouplingMatrix M) {
        if (S.size() < 2) return 0.0;
        List<String> list = new ArrayList<>(S);
        double sum = 0.0; int n=0;
        for (int i=0;i<list.size();i++) for (int j=i+1;j<list.size();j++) { sum+=M.similarity(list.get(i), list.get(j)); n++; }
        return (n==0)?0.0:sum/n;
    }
}
