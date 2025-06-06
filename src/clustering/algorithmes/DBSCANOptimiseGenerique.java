package clustering.algorithmes;

import clustering.AlgorithmeClusteringAbstrait;
import metriques.MetriqueDistance;
import donnees.PixelData;
import java.util.*;

/**
 * Version optimisée de DBSCAN générique avec index spatial.
 * Utilise une grille spatiale pour accélérer la recherche de voisins
 * quand on travaille avec des positions.
 *
 * @param <T> Le type de données à clustériser
 */
public class DBSCANOptimiseGenerique<T> extends AlgorithmeClusteringAbstrait<T> {

    private final double eps;
    private final int minPts;

    // Pour l'optimisation spatiale
    private boolean utilisationIndexSpatial = true;
    private Map<String, List<Integer>> grilleSpatiale;
    private int tailleGrille;

    // États des points
    private static final int NON_VISITE = -2;
    private static final int BRUIT = -1;

    public DBSCANOptimiseGenerique(double eps, int minPts) {
        super("DBSCAN Optimisé (eps=" + eps + ", minPts=" + minPts + ")");
        this.eps = eps;
        this.minPts = minPts;
        this.tailleGrille = (int) Math.ceil(eps);
    }

    /**
     * Active l'utilisation de l'index spatial pour les données de type PixelData.
     * Améliore significativement les performances pour le clustering par position.
     */
    public void activerIndexSpatial() {
        this.utilisationIndexSpatial = true;
    }

    @Override
    public int[] executer(T[] donnees, MetriqueDistance<T> metrique) {
        int n = donnees.length;
        int[] clusters = new int[n];
        Arrays.fill(clusters, NON_VISITE);

        // Construire l'index spatial si applicable
        if (utilisationIndexSpatial && donnees.length > 0 && donnees[0] instanceof PixelData) {
            construireGrilleSpatiale(donnees);
        }

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Trouver les voisins
            List<Integer> voisins;
            if (utilisationIndexSpatial && grilleSpatiale != null) {
                voisins = trouverVoisinsOptimise(donnees, i, metrique);
            } else {
                voisins = trouverVoisins(donnees, i, metrique);
            }

            if (voisins.size() < minPts) {
                clusters[i] = BRUIT;
            } else {
                expandCluster(donnees, clusters, i, voisins, clusterActuel, metrique);
                clusterActuel++;
            }
        }

        this.nombreClusters = clusterActuel;

        // Convertir les points de bruit
        for (int i = 0; i < n; i++) {
            if (clusters[i] == BRUIT) {
                clusters[i] = -1;
            }
        }

        return clusters;
    }

    /**
     * Construit une grille spatiale pour accélérer les recherches.
     */
    @SuppressWarnings("unchecked")
    private void construireGrilleSpatiale(T[] donnees) {
        grilleSpatiale = new HashMap<>();

        for (int i = 0; i < donnees.length; i++) {
            if (donnees[i] instanceof PixelData) {
                PixelData pixel = (PixelData) donnees[i];
                String cle = getCleGrille(pixel.getX(), pixel.getY());
                grilleSpatiale.computeIfAbsent(cle, k -> new ArrayList<>()).add(i);
            }
        }
    }

    /**
     * Génère une clé pour la cellule de grille.
     */
    private String getCleGrille(int x, int y) {
        int gx = x / tailleGrille;
        int gy = y / tailleGrille;
        return gx + "," + gy;
    }

    /**
     * Version optimisée de la recherche de voisins utilisant l'index spatial.
     */
    @SuppressWarnings("unchecked")
    private List<Integer> trouverVoisinsOptimise(T[] donnees, int pointIndex, MetriqueDistance<T> metrique) {
        List<Integer> voisins = new ArrayList<>();

        if (!(donnees[pointIndex] instanceof PixelData)) {
            return trouverVoisins(donnees, pointIndex, metrique);
        }

        PixelData pixel = (PixelData) donnees[pointIndex];
        int gx = pixel.getX() / tailleGrille;
        int gy = pixel.getY() / tailleGrille;
        int rayonGrille = (int) Math.ceil(eps / tailleGrille);

        // Vérifier uniquement les cellules voisines pertinentes
        for (int dx = -rayonGrille; dx <= rayonGrille; dx++) {
            for (int dy = -rayonGrille; dy <= rayonGrille; dy++) {
                String cle = (gx + dx) + "," + (gy + dy);
                List<Integer> pointsDansCellule = grilleSpatiale.get(cle);

                if (pointsDansCellule != null) {
                    for (int i : pointsDansCellule) {
                        if (metrique.calculerDistance(donnees[pointIndex], donnees[i]) <= eps) {
                            voisins.add(i);
                        }
                    }
                }
            }
        }

        return voisins;
    }

    /**
     * Recherche de voisins standard (sans optimisation).
     */
    private List<Integer> trouverVoisins(T[] donnees, int pointIndex, MetriqueDistance<T> metrique) {
        List<Integer> voisins = new ArrayList<>();
        T point = donnees[pointIndex];

        for (int i = 0; i < donnees.length; i++) {
            if (metrique.calculerDistance(point, donnees[i]) <= eps) {
                voisins.add(i);
            }
        }

        return voisins;
    }


    /**
     * Étend le cluster en ajoutant tous les points atteignables.
     */
    private void expandCluster(T[] donnees, int[] clusters, int pointIndex,
                               List<Integer> voisins, int clusterId,
                               MetriqueDistance<T> metrique) {
        clusters[pointIndex] = clusterId;

        // Utiliser une liste au lieu d'une queue pour de meilleures performances
        List<Integer> aTraiter = new ArrayList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        int index = 0;
        while (index < aTraiter.size()) {
            int voisinIndex = aTraiter.get(index++);

            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

                // Trouver les voisins du voisin
                List<Integer> voisinsDuVoisin;

                if (utilisationIndexSpatial && grilleSpatiale != null) {
                    voisinsDuVoisin = trouverVoisinsOptimise(donnees, voisinIndex, metrique);
                } else {
                    voisinsDuVoisin = trouverVoisins(donnees, voisinIndex, metrique);
                }

                if (voisinsDuVoisin.size() >= minPts) {
                    for (int nouveauVoisin : voisinsDuVoisin) {
                        if (!voisinsSet.contains(nouveauVoisin)) {
                            aTraiter.add(nouveauVoisin);
                            voisinsSet.add(nouveauVoisin);
                        }
                    }
                }
            } else if (clusters[voisinIndex] == BRUIT) {
                clusters[voisinIndex] = clusterId;
            }
        }
    }
}