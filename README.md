# 🌍 Détection de Biomes sur des Exoplanètes

> **SAÉ - Optimisation 2024**  
> Projet de détection automatique de biomes et d'écosystèmes sur des cartes d'exoplanètes à l'aide d'algorithmes de clustering. Tout est codé entièrement sans utilisation de bibliothèques

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 📖 Description (Sujet)

Suite à la mise en route du premier télescope à lentille gravitationnelle solaire (T-LGS) en juillet 2128, ce projet vise à analyser automatiquement les photographies d'exoplanètes pour détecter et classifier les différents biomes présents à leur surface.

Le programme une une IA qui :
- Filtrer et prétraiter les images
- Détecter les biomes par clustering de couleurs similaires
- Identifier les écosystèmes au sein de chaque biome
- Valider la qualité des résultats avec des indices statistiques

## 👥 Groupe

- **Ryan Korban**
- **Baptiste Hennequin**
- **Baptiste Delaborde**
- **Maxence Eva**

## 🚀 Fonctionnalités

### 1. **Prétraitement d'images**
- Filtres de flou (Gaussien et Moyenne)
- Homogénéisation des couleurs tout en préservant les frontières

### 2. **Détection de biomes**
- **Algorithmes disponibles** :
  - K-Means (recommandé pour les biomes)
  - DBSCAN
- **Métriques de couleur** :
  - CIELAB
  - CIE94 (recommandé)
  - Euclidienne RGB
  - Redmean

### 3. **Détection d'écosystèmes**
- Clustering spatial des pixels au sein de chaque biome
- **Algorithmes** :
  - DBSCAN Optimisé (avec grille spatiale) (recommandé pour les écosystèmes)
  - K-Means (pas opti)
  - DBSCAN Standard (lent... très lent...)

### 4. **Validation**
- **Indice de Davies-Bouldin** (K-Means)
- **Score de Silhouette** (DBSCAN)
- Rapports détaillés avec statistiques

### 5. **Visualisation**
- Vue globale des biomes détectés
- Biomes isolés sur fond clair
- Écosystèmes colorés par biome


## 📦 Installation

1. **Cloner le repository**
```bash
git clone https://github.com/korban2u/Clustering_Exoplanete.git
cd detection-biomes-exoplanetes
```

2. **Compiler le projet**
```bash
javac -d bin src/**/*.java
```
(ou lancer sur IntelliJ)

3. **Préparer les images**
   - Placer les images d'exoplanètes dans le dossier `./exoplanètes/`
   - Formats supportés : JPG, PNG

## 🎮 Utilisation

### Interface graphique
```bash
java -cp bin MainInterface
```

L'interface propose :
1. **Accueil** : Charger une image
2. **Filtre** : Appliquer un prétraitement
3. **Biomes** : Détecter et visualiser les biomes
4. **Écosystèmes** : Analyser les écosystèmes par biome
5. **Export** : Sauvegarder tous les résultats

### Version console
```bash
java -cp bin MainConsole
```


## 📁 Structure du projet

```
src/
├── clustering/
│   ├── algorithmes/        # K-Means, DBSCAN, DBSCAN Optimisé
│   ├── centroides/         # Calculateurs de centroïdes
│   └── ClusteringManager   # Gestionnaire principal
├── filtres/
│   ├── FiltreFlouGaussien  # Filtre gaussien
│   └── FiltreFlouMoyenne   # Filtre moyenne
├── metriques/
│   ├── couleur/            # Métriques pour les couleurs (Adapte les normes faites en TP)
│   └── position/           # Métriques spatiales (On a juste l'euclidienne)
├── normeCouleurs/
│   ├── NormeCielab         # Distance CIELAB
│   ├── NormeCie94          # Distance CIE94
│   ├── NormeReadMan          #Distance ReadMan
│   └── NormeEuclidienne    # Distance RGB simple
├── outils/
│   ├── OutilsImage         # Manipulation d'images
│   └── PixelData           # Structure de données pixel pour faciliter le code
├── validation/
│   ├── DaviesBouldinIndex  # Validation K-Means
│   └── SilhouetteScore     # Validation DBSCAN
├── visualisation/
│   ├── VisualisationBiomes      # Affichage des biomes
│   └── VisualisationEcosystemes # Affichage des écosystèmes
├── MainInterface.java      # Interface graphique
└── MainConsole.java        # Interface console
```

### Structure de sortie
```
resultats/
└── NomPlanete/
    ├── biomes/
    │   ├── biomes_detectes.jpg
    │   ├── biome_00_tundra.jpg
    │   ├── biome_01_foret.jpg
    │   └── rapport_biomes.txt
    └── ecosystemes/
        ├── biome_00_tundra/
        │   ├── ecosystemes.jpg
        │   └── rapport_ecosystemes.txt
        └── ...
```

## 🔧 Configuration recommandée

### Pour les biomes
- **Filtre** : Gaussien 5x5, sigma 1.5
- **Algorithme** : K-Means avec 5-8 clusters
- **Métrique** : CIE94 (meilleure perception des couleurs)

### Pour les écosystèmes  
- **Algorithme** : DBSCAN Optimisé
- **Epsilon** : 50 pixels
- **MinPts** : 30 points

## 📊 Performances

- **K-Means** : O(n·k·i) où n=pixels, k=clusters, i=itérations
- **DBSCAN Standard** : O(n²)
- **DBSCAN Optimisé** : O(n·log n) avec grille spatiale

## 📝 License

Projet sous la licence MIT. Voir `LICENSE` pour plus d'informations.
