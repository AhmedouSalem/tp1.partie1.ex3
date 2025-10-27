package tp1.partie1.ex3.service;

import tp1.partie1.ex3.model.Dendrogram;

import java.util.*;

/** Agglomératif bottom-up (average-link). On fusionne la paire la plus similaire à chaque étape. */
public class HierarchicalClustering {

    public Dendrogram fit(CouplingMatrix M) {
        // file de clusters courants
        List<Dendrogram> clusters = new ArrayList<>();
        for (String c : M.classes()) clusters.add(Dendrogram.leaf(c));

        while (clusters.size() > 1) {
            double bestSim = -1.0; int bi=-1, bj=-1;
            for (int i=0;i<clusters.size();i++) {
                for (int j=i+1;j<clusters.size();j++) {
                    double s = M.similarity(clusters.get(i).classes, clusters.get(j).classes);
                    if (s > bestSim) { bestSim = s; bi=i; bj=j; }
                }
            }
            // fusion des deux plus proches (max similarité)
            Dendrogram a = clusters.get(bi), b = clusters.get(bj);
            double height = 1.0 - Math.max(0.0, bestSim);  // dissimilarité
            Dendrogram merged = Dendrogram.join(a, b, height);

            // remplacer i par merged, retirer j
            clusters.set(bi, merged);
            clusters.remove(bj);
        }
        return clusters.get(0);
    }
}
