package clusters;

import java.util.*;

/**
 * Implémentation de l'algorithme DBSCAN pour la détection d'écosystèmes.
 * Adapté pour traiter des positions de pixels (x, y) avec la distance euclidienne.
 */
public class DBSCAN implements AlgoClustering {

    private double eps;  // Rayon de voisinage
    private int minPts;  // Nombre minimum de points pour former un cluster
    private int nombreClusters;

    // États des points
    private static final int NON_VISITE = 0;
    private static final int BRUIT = -1;

    /**
     * Constructeur
     * @param eps Rayon de voisinage (distance maximale entre points)
     * @param minPts Nombre minimum de points pour former un cluster
     */
    public DBSCAN(double eps, int minPts) {
        this.eps = eps;
        this.minPts = minPts;
    }

    @Override
    public int[] classifier(int[][] objets) {
        int n = objets.length;
        int[] clusters = new int[n];
        Arrays.fill(clusters, NON_VISITE);

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Trouver les voisins du point i
            List<Integer> voisins = regionQuery(objets, i);

            if (voisins.size() < minPts) {
                // Point de bruit
                clusters[i] = BRUIT;
            } else {
                // Créer un nouveau cluster
                expandCluster(objets, clusters, i, voisins, clusterActuel);
                clusterActuel++;
            }
        }

        this.nombreClusters = clusterActuel;

        // Convertir les points de bruit en cluster spécial si nécessaire
        // ou les laisser comme -1
        for (int i = 0; i < n; i++) {
            if (clusters[i] == BRUIT) {
                clusters[i] = -1; // ou clusterActuel si on veut un cluster "bruit"
            }
        }

        return clusters;
    }

    /**
     * Trouve tous les points dans le rayon eps du point donné
     */
    private List<Integer> regionQuery(int[][] objets, int pointIndex) {
        List<Integer> voisins = new ArrayList<>();
        int[] point = objets[pointIndex];

        for (int i = 0; i < objets.length; i++) {
            if (distanceEuclidienne(point, objets[i]) <= eps) {
                voisins.add(i);
            }
        }

        return voisins;
    }

    /**
     * Étend le cluster en ajoutant tous les points atteignables
     */
    private void expandCluster(int[][] objets, int[] clusters, int pointIndex,
                               List<Integer> voisins, int clusterId) {
        clusters[pointIndex] = clusterId;

        Queue<Integer> aTraiter = new LinkedList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        while (!aTraiter.isEmpty()) {
            int voisinIndex = aTraiter.poll();

            // Si le point n'a pas été visité
            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

                // Trouver les voisins du voisin
                List<Integer> voisinsDuVoisin = regionQuery(objets, voisinIndex);

                // Si c'est un core point, ajouter ses voisins à traiter
                if (voisinsDuVoisin.size() >= minPts) {
                    for (int nouveauVoisin : voisinsDuVoisin) {
                        if (!voisinsSet.contains(nouveauVoisin)) {
                            aTraiter.add(nouveauVoisin);
                            voisinsSet.add(nouveauVoisin);
                        }
                    }
                }
            } else if (clusters[voisinIndex] == BRUIT) {
                // Si c'était un point de bruit, l'ajouter au cluster
                clusters[voisinIndex] = clusterId;
            }
        }
    }

    /**
     * Calcule la distance euclidienne entre deux points
     */
    private double distanceEuclidienne(int[] p1, int[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public int getNombreClusters() {
        return nombreClusters;
    }

    @Override
    public String getNomAlgorithme() {
        return "DBSCAN";
    }

    // Getters et setters pour les paramètres
    public double getEps() {
        return eps;
    }

    public void setEps(double eps) {
        this.eps = eps;
    }

    public int getMinPts() {
        return minPts;
    }

    public void setMinPts(int minPts) {
        this.minPts = minPts;
    }
}