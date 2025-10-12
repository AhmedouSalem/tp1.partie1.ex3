# 🧩 TP1 – Partie 2 : Construction du graphe d’appel d’une application Java

## 📘 Contexte

Ce projet s’inscrit dans la continuité de l’**exercice 3 de la partie 1 du TP**,  
où un outil d’analyse statique basé sur **Eclipse JDT** avait été développé pour extraire :
- la liste des classes et de leurs méthodes,
- la liste des appels effectués dans chaque méthode.

L’objectif de cette seconde partie est de **construire et visualiser le graphe d’appel**  
(_Call Graph_) d’une application Java, afin d’illustrer les relations entre les méthodes 
et les dépendances entre classes.

Chaque nœud du graphe représente une méthode (`Type::Méthode`),  
et chaque arête orientée symbolise un appel entre deux méthodes.

---

## ⚙️ Outils et technologies utilisées

| Outil / Librairie | Rôle |
|--------------------|------|
| **Eclipse JDT (Core)** | Analyse statique du code et génération de l’AST (Abstract Syntax Tree). |
| **Apache Commons IO** | Lecture récursive des fichiers `.java`. |
| **Graphviz** | Génération du graphe visuel à partir du fichier `.dot`. |
| **Java Swing** | Interface graphique (`CallGraphUI`) pour sélectionner un dossier source et afficher le graphe. |
| **Maven** | Gestion des dépendances et compilation du projet. |
| **Ubuntu/Linux** | Environnement de test et d’exécution. |

---

## 🧱 Architecture du projet

Le projet adopte une architecture modulaire en plusieurs packages clairement séparés :

```
tp1.partie1.ex3
│
├── gui        → Interface graphique et exécution principale (Application, CallGraphUI)
├── model      → Représentation du graphe (CallEdge, MethodRef)
├── parser     → Lecture des fichiers et création des AST (SourceScanner, CompilationUnitFactory)
├── report     → Gestion de la sortie (ConsoleReporter)
├── service    → Logique d’analyse et export du graphe (AnalysisService)
└── visitor    → Visiteurs JDT (TypeDeclarationVisitor, MethodInvocationVisitor)
```

Cette structure permet :
- une **clarté de la responsabilité** de chaque couche,
- une **extensibilité** pour d’autres formats d’export (JSON, UML, etc.),
- et une **réutilisation directe** du code développé à l’exercice précédent.

---

## 🧩 Principe de fonctionnement

### 1️⃣ Analyse du code source
- Le parser **Eclipse JDT** construit un arbre syntaxique (AST) pour chaque fichier `.java`.
- Le visiteur `TypeDeclarationVisitor` parcourt les classes et leurs méthodes.
- Le visiteur `MethodInvocationVisitor` identifie les appels de méthodes (internes et externes).

### 2️⃣ Construction du graphe
- Chaque appel est transformé en arête orientée `CallEdge(from, to)`.
- L’ensemble des arêtes est collecté par le service `AnalysisService`.
- Le graphe est exporté au format `callgraph.dot` et `callgraph.puml`.

### 3️⃣ Visualisation
- **Graphviz** génère une image (`callgraph.png`) via la commande :
  ```bash
  dot -Tpng target/callgraph.dot -o target/callgraph.png
  ```
- Les **flèches vertes** indiquent les appels internes au projet.
- Les **flèches grises pointillées** représentent les appels externes (JDK, bibliothèques).
- Une interface Swing permet d’afficher et de zoomer sur le graphe.

---

## 🧠 Extraits de code

### Création du CompilationUnit avec Eclipse JDT
```java
ASTParser parser = ASTParser.newParser(AST.JLS17);
parser.setKind(ASTParser.K_COMPILATION_UNIT);
parser.setSource(source.toCharArray());
parser.setResolveBindings(true);
parser.setBindingsRecovery(true);
parser.setEnvironment(null, new String[]{SOURCE_PATH}, null, true);
CompilationUnit cu = (CompilationUnit) parser.createAST(null);
```

### Export du graphe coloré
```java
boolean internal = projectTypes.contains(e.to().typeName());
String style = internal ? "color=green, penwidth=1.6"
                        : "color=gray50, style=dashed";
sb.append(from).append(" -> ").append(to).append(" [")
  .append(style).append("];\n");
```

---

## 🖼️ Résultat

### Graphe d’appel généré
<p align="center">
  <img src="https://github.com/AhmedouSalem/tp1.partie2.ex1/blob/main/%20images/viewSwing.png" alt="Interface Swing" width="700"/>
</p>

**Légende :**
- 🟢 flèches vertes → appels internes entre classes du projet  
- ⚫ flèches grises pointillées → appels externes (bibliothèques, API standard)

---

## 🪟 Interface graphique (optionnelle)

Une interface Swing (`CallGraphUI`) permet de sélectionner un dossier source et d’afficher 
le graphe d’appel sous forme d’image zoomable.

Lancer la classe :
```bash
mvn compile exec:java -Dexec.mainClass="tp1.partie1.ex3.gui.CallGraphUI"
```

---

## 📁 Exemple de sortie

```
Classe : Main
  Méthode : main
    → Appel : asList    (receveur : Arrays (static))
    → Appel : dance     (receveur : Dancer)
Classe : Breakdancer
  Méthode : dance
    → Appel : dance     (receveur : Dancer)
    → Appel : testThis  (receveur : Breakdancer)
    → Appel : println   (receveur : PrintStream)
```

---

## 🧩 Améliorations possibles

- Export du graphe en **JSON** ou **format interactif web** (D3.js, Cytoscape.js).  
- **Filtrage par package** ou **vue hiérarchique** des dépendances.  
- **Détection de cycles** et calcul de métriques (centralité, fan-in/fan-out).  
- **Intégration CI/CD** : exécution automatique après compilation Maven.

---

## 👨‍💻 Auteur

**Ahmedou Salem**  
Master 2 Génie Logiciel – Université de Montpellier  
📧 [ahmedou.salem@etu.umontpellier.fr](mailto:ahmedou.salem@etu.umontpellier.fr)  
🔗 [LinkedIn](https://www.linkedin.com/in/salem-ahmedou-ba5500244/) · [GitHub](https://github.com/AhmedouSalem)

---

## 🧾 Licence
Ce projet est fourni à des fins pédagogiques (TP Génie Logiciel, 2025).  
Licence : **MIT** – libre d’utilisation et de modification à des fins d’apprentissage.