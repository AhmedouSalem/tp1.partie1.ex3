package tp1.partie1.ex3.model;

import java.util.Objects;

/** Paire non ordonnée {A,B} avec A != B. */
public final class ClassPair {
    private final String a;
    private final String b;

    public ClassPair(String c1, String c2) {
        if (c1.equals(c2)) throw new IllegalArgumentException("ClassPair requires A != B");
        // ordre canonique pour l'égalité/hash
        if (c1.compareTo(c2) < 0) { this.a = c1; this.b = c2; }
        else { this.a = c2; this.b = c1; }
    }
    public String a() { return a; }
    public String b() { return b; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassPair)) return false;
        ClassPair that = (ClassPair) o;
        return a.equals(that.a) && b.equals(that.b);
    }
    @Override public int hashCode() { return Objects.hash(a, b); }
    @Override public String toString() { return "{" + a + ", " + b + "}"; }
}
