package clusters;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation de l'algorithme DBSCAN pour le clustering basé sur la densité.
 * Particulièrement adapté pour détecter des clusters de formes arbitraires et gérer le bruit.
 */
public class DBSCAN implements AlgoClustering {

    private double eps;          // Rayon de voisinage
    private int minPts;          // Nombre minimum de points pour former un cluster
    private int nombreClusters;  // Nombre de clusters trouvés

    // États des points
    private static final int NON_TRAITE = -2;
    private static final int BRUIT = -1;

    /**
     * Constructeur pour DBSCAN.
     * @param eps Rayon de voisinage (distance maximale entre points voisins)
     * @param minPts Nombre minimum de points pour former un cluster
     */
    public DBSCAN(double eps, int minPts) {
        this.eps = eps;
        this.minPts = minPts;
        this.nombreClusters = 0;
    }

    @Override
    public int[] classifier(int[][] donnees) {
        int n = donnees.length;
        int[] affectations = new int[n];
        boolean[] traite = new boolean[n];

        // Initialiser tous les points comme non traités
        for (int i = 0; i < n; i++) {
            affectations[i] = NON_TRAITE;
            traite[i] = false;
        }

        int clusterCourant = 0;

        // Parcourir tous les points
        for (int i = 0; i < n; i++) {
            if (!traite[i]) {
                traite[i] = true;

                // Trouver les voisins
                List<Integer> voisins = regionQuery(donnees, i, eps);

                if (voisins.size() >= minPts) {
                    // Créer un nouveau cluster
                    expandCluster(donnees, affectations, traite, i, voisins, clusterCourant);
                    clusterCourant++;
                } else {
                    // Marquer comme bruit
                    affectations[i] = BRUIT;
                }
            }
        }

        this.nombreClusters = clusterCourant;

        // Convertir les points de bruit en -1 pour être compatible avec l'interface
        // et remettre les clusters à partir de 0
        for (int i = 0; i < n; i++) {
            if (affectations[i] == BRUIT) {
                affectations[i] = -1;
            }
        }

        return affectations;
    }

    /**
     * Expande un cluster à partir d'un point core et ses voisins.
     */
    private void expandCluster(int[][] donnees, int[] affectations, boolean[] traite,
                               int pointIndex, List<Integer> voisins, int clusterId) {
        // Ajouter le point au cluster
        affectations[pointIndex] = clusterId;

        // Parcourir tous les voisins
        int i = 0;
        while (i < voisins.size()) {
            int voisinIndex = voisins.get(i);

            if (!traite[voisinIndex]) {
                traite[voisinIndex] = true;

                // Trouver les voisins du voisin
                List<Integer> voisinsVoisin = regionQuery(donnees, voisinIndex, eps);

                if (voisinsVoisin.size() >= minPts) {
                    // Ajouter les nouveaux voisins à la liste
                    for (int nouveauVoisin : voisinsVoisin) {
                        if (!voisins.contains(nouveauVoisin)) {
                            voisins.add(nouveauVoisin);
                        }
                    }
                }
            }

            // Si le point n'appartient à aucun cluster, l'ajouter au cluster courant
            if (affectations[voisinIndex] < 0) {
                affectations[voisinIndex] = clusterId;
            }

            i++;
        }
    }

    /**
     * Trouve tous les points dans le rayon eps d'un point donné.
     * Pour la détection des écosystèmes, on utilise la distance euclidienne
     * sur les coordonnées (x,y) des pixels.
     * OPTIMISATION : On arrête la recherche dès qu'on dépasse eps
     */
    private List<Integer> regionQuery(int[][] donnees, int pointIndex, double eps) {
        List<Integer> voisins = new ArrayList<>();
        double epsCarree = eps * eps; // Éviter de calculer la racine carrée

        for (int i = 0; i < donnees.length; i++) {
            if (i != pointIndex) {
                // Calcul optimisé de la distance au carré
                double distanceCarree = calculerDistanceCarree(donnees[pointIndex], donnees[i]);
                if (distanceCarree <= epsCarree) {
                    voisins.add(i);
                }
            }
        }

        return voisins;
    }

    /**
     * Calcule la distance au carré entre deux points (plus rapide, pas de sqrt).
     */
    private double calculerDistanceCarree(int[] point1, int[] point2) {
        double somme = 0;
        for (int i = 0; i < point1.length; i++) {
            double diff = point1[i] - point2[i];
            somme += diff * diff;
        }
        return somme;
    }

    /**
     * Calcule la distance entre deux points.
     * Pour les positions de pixels : distance euclidienne simple.
     * Pour les couleurs RGB : on pourrait utiliser une norme de couleur.
     */
    private double calculerDistance(int[] point1, int[] point2) {
        return Math.sqrt(calculerDistanceCarree(point1, point2));
    }

    @Override
    public int getNombreClusters() {
        return nombreClusters;
    }

    @Override
    public String getNomAlgorithme() {
        return "DBSCAN";
    }

    /**
     * Retourne le nombre de points considérés comme du bruit.
     */
    public int getNombrePointsBruit(int[] affectations) {
        int compteur = 0;
        for (int affectation : affectations) {
            if (affectation == -1) {
                compteur++;
            }
        }
        return compteur;
    }
}