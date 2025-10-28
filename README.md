# TP2 — Exercice 2 : Identification de modules (clustering hiérarchique)

## 🎯 Objectif

L’objectif de cet exercice est d’**identifier automatiquement des modules cohérents** 
à partir du **graphe de couplage entre classes** construit dans l’exercice précédent (TP2 — Ex. 1).  
Chaque module regroupe des classes fortement couplées entre elles, permettant 
de dégager une vision **architecturale** de l’application.

---

## 🧠 Contexte théorique

Le regroupement de classes en modules à partir de leurs relations de couplage 
est un **problème combinatoire** :  
pour $M$ classes, le nombre de partitions possibles est donné par le **nombre de Bell** :

| M | Nombre de partitions (Bell) |
|---|-----------------------------:|
| 5 | 52                          |
| 10 | 115 975                    |
| 15 | 1 382 958 545 091          |

Une recherche exhaustive est donc impossible : le problème est **NP-difficile**.  
Nous utilisons ici une **heuristique gloutonne hiérarchique** : un 
**clustering agglomératif à liaison moyenne (average-link)**.

---

## ⚙️ Principe du raisonnement

1. **Calculer la matrice de similarité** `S[A][B] = weight(A,B)` à partir du graphe de couplage.
2. **Initialiser** un cluster par classe (feuille du dendrogramme).
3. **Fusionner** à chaque itération les deux clusters les plus similaires.
4. **Construire le dendrogramme** hiérarchique des fusions successives.
5. **Découper le dendrogramme** selon :
   - un **seuil de cohésion interne** `CP` (moyenne du couplage intra-module),
   - la contrainte `≤ M/2` modules (M = nombre de classes).

---

## 🧩 Structure logicielle du projet

Le projet est structuré dans le package `tp1.partie1.ex3` :

| Package / Classe | Rôle principal |
|------------------|----------------|
| **model/** | Structures de données : `Dendrogram`, `CouplingEdge`, `ClassPair`. |
| **service/** | Calculs principaux : `CouplingMatrix`, `HierarchicalClustering`, `ModuleExtractor`. |
| **report/** | Exports : `DendrogramDotExporter`, `ModulesCsvExporter`, `ModulesDotExporter`. |
| **gui/** | Interface graphique complète `AnalyzerUI`. |

### 🔍 Description rapide des classes

#### `CouplingMatrix`
Construit la matrice de similarité entre classes et calcule la moyenne entre groupes (average-link).

#### `HierarchicalClustering`
Implémente le clustering agglomératif :

```java
while (clusters.size() > 1) {
    double s = M.similarity(A,B);
    double h = 1 - s;
    clusters.merge(A,B);
}
```

#### `Dendrogram`
Représente les fusions successives (chaque nœud contient les classes fusionnées et la hauteur).

#### `ModuleExtractor`
Découpe l’arbre en modules selon :
- le seuil `CP` (cohésion interne minimale),
- la contrainte `≤ M/2` modules.

#### `ModulesDotExporter` et `ModulesCsvExporter`
Génèrent respectivement :
- le graphe **DOT/PNG** coloré par module,
- le fichier **CSV** récapitulatif :  
  `module_id, avgSimilarity, classes`.

#### `AnalyzerUI`
Interface Swing permettant :
- de choisir un dossier `src/`,
- de lancer l’analyse complète (Appel → Couplage → Clustering),
- de régler le seuil `CP`,
- d’afficher tous les résultats via des onglets :
  - **Call Graph (PNG)**
  - **Coupling (PNG / Table)**
  - **Dendrogram (PNG)**
  - **Modules (PNG / Table)**

---

## 🚀 Exécution

### ▶️ Ligne de commande

```bash
mvn -q -DskipTests compile exec:java   -Dexec.mainClass="tp1.partie1.ex3.gui.ClusteringApp"   -Dexec.args="/chemin/vers/src 0.10"
```

### ▶️ Interface graphique

```bash
mvn -q -DskipTests compile exec:java   -Dexec.mainClass="tp1.partie1.ex3.gui.AnalyzerUI"
```

> ⚠️ Nécessite [Graphviz](https://graphviz.org/download/) pour générer les images PNG.  
> Sous Ubuntu :  
> ```bash
> sudo apt-get install graphviz
> ```

---

## 📦 Fichiers produits (dossier `target/`)

| Fichier | Description |
|----------|-------------|
| `callgraph.dot` / `.png` | Graphe d’appel entre méthodes |
| `coupling.csv` / `.dot` / `.png` | Graphe de couplage pondéré entre classes |
| `dendrogram.dot` / `.png` | Arbre hiérarchique des fusions |
| `modules.csv` | Tableau des modules extraits |
| `modules.dot` / `.png` | Graphe coloré des modules |

---

## 📊 Exemple de résultats

### Graphe de couplage
![Coupling Graph](images/coupling.png)

### Dendrogramme hiérarchique
![Dendrogram](images/dendrogram.png)

### Graphe des modules colorés
![Modules](images/modules.png)

### Tableau des modules extraits (extrait du `modules.csv`)
| module_id | avgSimilarity | classes |
|------------|---------------|----------|
| 1 | 0.500000 | Main Dancer Breakdancer |
| 2 | 0.000000 | Example ElectricBoogieDancer |

---

## 💬 Interprétation

- Les classes `Main`, `Dancer` et `Breakdancer` forment un **module fortement cohésif**  
  (couplage moyen 0.5).
- `Example` et `ElectricBoogieDancer` constituent un **module faiblement lié** aux autres.
- Le découpage satisfait :
  - $|modules| \le M/2$
  - cohésion intra-module $\ge CP$
  - modules = branches disjointes du dendrogramme.

---

## ⏱️ Complexité et discussion

- Clustering agglomératif naïf : **O(M³)**  
  (acceptable pour de petits projets académiques).
- Découpage sous contraintes : **O(M²)**.
- L’approche est **heuristique**, non optimale globalement,
  mais interprétable et extensible.

### 🔮 Améliorations possibles
- Intégrer des poids dynamiques (fréquences d’exécution).
- Utiliser des heuristiques avancées (algorithmes génétiques, Louvain, spectral clustering).
- Exporter les modules vers un outil de visualisation interactif.

---