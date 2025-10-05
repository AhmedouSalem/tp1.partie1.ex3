# 🧩 TP1 – Analyse statique et graphe d’appel (Évolution et Restructuration des Logiciels)

## 🎯 Objectif du projet
Ce projet a été réalisé dans le cadre de l’UE **HAI913I – Évolution et Restructuration des Logiciels (Master 2 Génie Logiciel)**.  
L’objectif est de développer une application Java capable :
- d’analyser statiquement le code source d’un projet orienté objet ;
- d’extraire des **métriques logicielles** (nombre de classes, méthodes, lignes de code, etc.) ;
- et de générer le **graphe d’appel** des méthodes à partir des résultats de l’analyse.

Le tout est réalisé à l’aide de l’API **Eclipse JDT (Java Development Tools)** pour le parsing de code Java.

---

## 🏗️ Structure du projet
```
tp1.partie1.ex3/
├── src/main/java/
│   ├── tp1/partie1/ex3/gui/
│   │   ├── Main.java               # Application principale (analyse + affichage)
│   │   └── MainCallGraph.java      # Génération du graphe d’appel
│   ├── tp1/partie1/ex3/model/      # Classes de données : ClassInfo, MethodInfo, MethodCall
│   ├── tp1/partie1/ex3/service/    # Analyseur principal (ClassAnalysisService)
│   ├── tp1/partie1/ex3/util/       # Scanner de fichiers sources (SourceScanner)
│   ├── tp1/partie1/ex3/visitor/    # Visiteur AST pour méthodes et appels
│   └── tp1/partie1/ex3/graph/      # Génération du graphe (CallGraph, Builder, DotExporter)
│
├── pom.xml                         # Configuration Maven
├── README.md                       # Ce fichier
└── images/                         # Captures et graphes (facultatif)
```

---

## ⚙️ Prérequis
- **Java 17 ou supérieur** (testé avec OpenJDK 21)
- **Maven 3.8+**
- (Optionnel) **Graphviz** pour visualiser le graphe d’appel

### Vérifier votre environnement :
```bash
java -version
mvn -version
```

### Installer Graphviz (Linux / Ubuntu)
```bash
sudo apt install graphviz
```

---

## 🚀 Installation

1. **Cloner ou copier le projet**
   ```bash
   git clone https://github.com/votre-repo/tp1-analyse-jdt.git
   cd tp1-analyse-jdt
   ```

2. **Compiler le projet**
   ```bash
   mvn clean package
   ```

3. **(Optionnel)** Ouvrir le projet dans **Eclipse** ou **IntelliJ IDEA**  
   en important le répertoire comme un projet **Maven existant**.

---

## 💡 Utilisation

### ▶️ 1. Exécution de l’analyse statique

Cette exécution parcourt un dossier source, analyse les fichiers `.java` et affiche :
- le nombre de classes, méthodes, lignes de code ;
- les moyennes et statistiques globales.

Commande (en ligne de commande ou dans l’IDE) :
```bash
mvn exec:java -Dexec.mainClass="tp1.partie1.ex3.gui.Main"               -Dexec.args="/chemin/vers/ton/projet/src"
```

Exemple de sortie :
```
Classe : project.exemple.etude.Main
  Méthode : main(java.lang.String[])
    Appelle : asList   [Type receveur : java.util.Arrays]
    Appelle : dance    [Type receveur : project.exemple.etude.Dancer]

Classe : Dancer
  Méthode : dance()
    Appelle : println  [Type receveur : java.io.PrintStream]
    Appelle : toString [Type receveur : java.lang.Object]
```

---

### 🧭 2. Génération du graphe d’appel

L’exécution de `MainCallGraph` génère automatiquement :
- un fichier **`callgraph.dot`** (format Graphviz),
- et éventuellement un **fichier image** (si Graphviz est installé).

Commande :
```bash
mvn exec:java -Dexec.mainClass="tp1.partie1.ex3.gui.MainCallGraph"               -Dexec.args="/chemin/vers/ton/projet/src"
```

Sortie attendue :
```
✅ Fichier DOT généré → /.../callgraph.dot
   dot -Tpng callgraph.dot -o callgraph.png
```

### Visualiser le graphe
```bash
dot -Tpng callgraph.dot -o callgraph.png
xdg-open callgraph.png
```

Exemple de rendu :
<p align="center">
  <img src="https://github.com/AhmedouSalem/tp1.partie1.ex3/blob/main/callgraph.png" alt="Interface Swing" width="700"/>
</p>
<p align="center">
  <img src="https://github.com/AhmedouSalem/tp1.partie1.ex3/blob/main/callgraph1.png" alt="Interface Swing" width="700"/>
</p>

---

## 🧠 Principaux composants

| Package / Classe | Rôle |
|------------------|------|
| `parser` / `visitor` | Utilisent **Eclipse JDT** pour construire et parcourir l’AST |
| `model.ClassInfo`, `MethodInfo`, `MethodCall` | Modélisent les éléments extraits du code |
| `service.ClassAnalysisService` | Lance l’analyse complète et agrège les résultats |
| `graph.CallGraphBuilder` | Construit un graphe d’appel orienté à partir des appels détectés |
| `graph.DotExporter` | Produit un fichier `.dot` compatible avec **Graphviz** |
| `gui.Main`, `MainCallGraph` | Points d’entrée (analyse métriques / graphe d’appel) |

---

## 🧩 Exemple rapide (résumé)

**Entrée :**  
Fichiers Java du projet `project.exemple.etude`

**Sortie :**  
- Résumé des métriques en console  
- Fichier `callgraph.dot` (et `callgraph.png` si Graphviz installé)

---

## 📚 Références
- API officielle : [Eclipse JDT – org.eclipse.jdt.core.dom](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/package-summary.html)
- Outil Graphviz : [https://graphviz.org](https://graphviz.org)
- TP : *UE HAI913I – Évolution et Restructuration des Logiciels (Université de Montpellier)*

---

## 👤 Auteur
**Ahmedou Salem**  
Master 2 Génie Logiciel – Université de Montpellier  
📅 Octobre 2025  