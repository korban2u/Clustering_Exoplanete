package clustering.algorithmes;

import metriques.MetriqueDistance;
import outils.PixelData;
import java.util.*;

/**
 * Implémentation de l'algorithme DBSCAN pour PixelData.
 */
public class DBSCAN extends AlgorithmeClusteringAbstrait {

    private final double eps;
    private final int minPts;

    // États des points
    private static final int NON_VISITE = -2;
    private static final int BRUIT = -1;

    public DBSCAN(double eps, int minPts) {
        super("DBSCAN (eps=" + eps + ", minPts=" + minPts + ")");
        this.eps = eps;
        this.minPts = minPts;
    }

    @Override
    public int[] executer(PixelData[] donnees, MetriqueDistance metrique) {
        int n = donnees.length;
        int[] clusters = new int[n];
        Arrays.fill(clusters, NON_VISITE);

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Trouver les voisins
            List<Integer> voisins = trouverVoisins(donnees, i, metrique);

            if (voisins.size() < minPts) {
                clusters[i] = BRUIT;
            } else {
                expandCluster(donnees, clusters, i, voisins, clusterActuel, metrique);
                clusterActuel++;
            }
        }

        this.nombreClusters = clusterActuel;

        // Convertir les points de bruit en -1
        for (int i = 0; i < n; i++) {
            if (clusters[i] == BRUIT) {
                clusters[i] = -1;
            }
        }

        return clusters;
    }

    /**
     * Trouve tous les points dans le rayon eps du point donné.
     */
    private List<Integer> trouverVoisins(PixelData[] donnees, int pointIndex,
                                         MetriqueDistance metrique) {
        List<Integer> voisins = new ArrayList<>();
        PixelData point = donnees[pointIndex];

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
    private void expandCluster(PixelData[] donnees, int[] clusters, int pointIndex,
                               List<Integer> voisins, int clusterId,
                               MetriqueDistance metrique) {
        clusters[pointIndex] = clusterId;

        Queue<Integer> aTraiter = new LinkedList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        while (!aTraiter.isEmpty()) {
            int voisinIndex = aTraiter.poll();

            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

                // Trouver les voisins du voisin
                List<Integer> voisinsDuVoisin = trouverVoisins(donnees, voisinIndex, metrique);

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

    // Getters pour permettre l'accès aux paramètres
    public double getEps() {
        return eps;
    }

    public int getMinPts() {
        return minPts;
    }
}