package tp1.partie1.ex3.model;

import java.util.*;

/** Nœud de dendrogramme (clustering hiérarchique). */
public class Dendrogram {
    public final Dendrogram left, right;   // null si feuille
    public final Set<String> classes;      // ensemble des classes dans ce nœud
    public final double height;            // dissimilarité au moment de la fusion (1 - similarité)

    private Dendrogram(Dendrogram l, Dendrogram r, Set<String> cls, double h) {
        this.left = l; this.right = r; this.classes = Collections.unmodifiableSet(cls); this.height = h;
    }
    /** feuille */
    public static Dendrogram leaf(String cls) { return new Dendrogram(null, null, Set.of(cls), 0.0); }
    /** fusion */
    public static Dendrogram join(Dendrogram a, Dendrogram b, double height) {
        Set<String> all = new LinkedHashSet<>(a.classes); all.addAll(b.classes);
        return new Dendrogram(a, b, all, height);
    }
    public boolean isLeaf() { return left == null && right == null; }
}
