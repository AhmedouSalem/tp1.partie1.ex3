package tp1.partie1.ex3.service;

import tp1.partie1.ex3.model.CallEdge;
import tp1.partie1.ex3.model.ClassPair;
import tp1.partie1.ex3.model.CouplingEdge;

import java.util.*;
import java.util.stream.Collectors;

/** Calcule le couplage binaire entre classes (non orienté) à partir des CallEdge. */
public class CouplingAnalyzer {

    /**
     * @param callEdges      arêtes méthode→méthode (déjà extraites par AnalysisService)
     * @param projectTypes   ensemble des classes internes (pour filtrer le JDK/lib)
     * @return liste d'arêtes de couplage (classe↔classe) avec poids normalisé ∈ (0,1]
     */
    public List<CouplingEdge> compute(Collection<CallEdge> callEdges, Set<String> projectTypes) {
        // 1) Comptage brut des appels inter-classes internes
        Map<ClassPair, Integer> counts = new LinkedHashMap<>();
        int totalInterClassCalls = 0;

        for (CallEdge e : callEdges) {
            String fromCls = e.from().typeName();
            String toCls   = e.to().typeName();
            if (fromCls.equals(toCls)) continue;                      // pas de A==B
            if (!projectTypes.contains(fromCls) || !projectTypes.contains(toCls)) continue; // internes only

            ClassPair pair = new ClassPair(fromCls, toCls);
            counts.merge(pair, 1, Integer::sum);
            totalInterClassCalls++;
        }

        if (totalInterClassCalls == 0) return List.of();

        // 2) Normalisation -> poids
        List<CouplingEdge> edges = new ArrayList<>();
        for (Map.Entry<ClassPair, Integer> e : counts.entrySet()) {
            double w = e.getValue() / (double) totalInterClassCalls;
            edges.add(new CouplingEdge(e.getKey().a(), e.getKey().b(), e.getValue(), w));
        }

        // tri décroissant par poids
        edges.sort(Comparator.comparingDouble(CouplingEdge::weight).reversed()
                             .thenComparing(CouplingEdge::classA)
                             .thenComparing(CouplingEdge::classB));
        return edges;
    }

    /** pour un CSV ou un affichage console. */
    public String toCsv(List<CouplingEdge> edges) {
        String header = "classA,classB,count,weight\n";
        String body = edges.stream()
                .map(e -> e.classA() + "," + e.classB() + "," + e.count() + "," + String.format(Locale.US, "%.6f", e.weight()))
                .collect(Collectors.joining("\n"));
        return header + body + "\n";
    }
}
