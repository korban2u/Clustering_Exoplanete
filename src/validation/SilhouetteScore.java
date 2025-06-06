package validation;

import clustering.ClusteringManager.ResultatClustering;
import donnees.PixelData;
import metriques.MetriqueDistance;

import java.awt.image.BufferedImage;

/**
 * Implémentation du coefficient de silhouette pour évaluer la qualité du clustering.
 * Particulièrement adapté pour DBSCAN.
 * Valeur entre -1 et 1 : proche de 1 = bon clustering, proche de -1 = mauvais.
 */
public class SilhouetteScore {

    /**
     * Calcule le score de silhouette moyen pour tout le clustering.
     *
     * @param resultat Le résultat du clustering
     * @param metrique La métrique de distance utilisée
     * @return Le score de silhouette moyen (entre -1 et 1)
     */
    public double calculer(ResultatClustering resultat, MetriqueDistance<PixelData> metrique) {
        if (resultat.nombreClusters <= 1) return 0.0;

        double sommeSilhouettes = 0.0;
        int compteurPoints = 0;

        // Pour chaque point qui n'est pas du bruit
        for (int i = 0; i < resultat.affectations.length; i++) {
            if (resultat.affectations[i] >= 0) { // Ignorer les points de bruit (-1)
                double silhouette = calculerSilhouettePoint(i, resultat, metrique);
                sommeSilhouettes += silhouette;
                compteurPoints++;
            }
        }

        if (compteurPoints == 0) return 0.0;

        return sommeSilhouettes / compteurPoints;
    }

    /**
     * Calcule le coefficient de silhouette pour un point spécifique.
     * s(i) = (b(i) - a(i)) / max(a(i), b(i))
     */
    private double calculerSilhouettePoint(int indexPoint,
                                           ResultatClustering resultat,
                                           MetriqueDistance<PixelData> metrique) {
        int clusterPoint = resultat.affectations[indexPoint];
        PixelData point = resultat.pixels[indexPoint];

        // Calculer a(i) : distance moyenne aux points du même cluster
        double a = calculerDistanceMoyenneIntraCluster(point, indexPoint, clusterPoint, resultat, metrique);

        // Calculer b(i) : distance moyenne minimale aux points des autres clusters
        double b = calculerDistanceMoyenneInterClusterMin(point, clusterPoint, resultat, metrique);

        // Si le point est seul dans son cluster
        if (a == 0 && b == 0) return 0.0;

        // Calculer le coefficient de silhouette
        double max = Math.max(a, b);
        if (max == 0) return 0.0;

        return (b - a) / max;
    }

    /**
     * Calcule la distance moyenne d'un point aux autres points de son cluster.
     */
    private double calculerDistanceMoyenneIntraCluster(PixelData point,
                                                       int indexPoint,
                                                       int cluster,
                                                       ResultatClustering resultat,
                                                       MetriqueDistance<PixelData> metrique) {
        double sommeDistances = 0.0;
        int compteur = 0;

        for (int i = 0; i < resultat.affectations.length; i++) {
            if (i != indexPoint && resultat.affectations[i] == cluster) {
                sommeDistances += metrique.calculerDistance(point, resultat.pixels[i]);
                compteur++;
            }
        }

        if (compteur == 0) return 0.0;
        return sommeDistances / compteur;
    }

    /**
     * Calcule la distance moyenne minimale aux clusters voisins.
     */
    private double calculerDistanceMoyenneInterClusterMin(PixelData point,
                                                          int clusterPoint,
                                                          ResultatClustering resultat,
                                                          MetriqueDistance<PixelData> metrique) {
        double distanceMin = Double.MAX_VALUE;

        // Pour chaque autre cluster
        for (int cluster = 0; cluster < resultat.nombreClusters; cluster++) {
            if (cluster != clusterPoint) {
                double distanceMoyenne = calculerDistanceMoyenneVersCluster(point, cluster, resultat, metrique);

                if (distanceMoyenne > 0 && distanceMoyenne < distanceMin) {
                    distanceMin = distanceMoyenne;
                }
            }
        }

        return distanceMin == Double.MAX_VALUE ? 0.0 : distanceMin;
    }

    /**
     * Calcule la distance moyenne d'un point vers tous les points d'un cluster donné.
     */
    private double calculerDistanceMoyenneVersCluster(PixelData point,
                                                      int cluster,
                                                      ResultatClustering resultat,
                                                      MetriqueDistance<PixelData> metrique) {
        double sommeDistances = 0.0;
        int compteur = 0;

        for (int i = 0; i < resultat.affectations.length; i++) {
            if (resultat.affectations[i] == cluster) {
                sommeDistances += metrique.calculerDistance(point, resultat.pixels[i]);
                compteur++;
            }
        }

        if (compteur == 0) return 0.0;
        return sommeDistances / compteur;
    }

    /**
     * Calcule le score de silhouette pour chaque cluster individuellement.
     * Utile pour identifier les clusters mal formés.
     */
    public double[] calculerParCluster(ResultatClustering resultat, MetriqueDistance<PixelData> metrique) {
        double[] scoresParCluster = new double[resultat.nombreClusters];
        int[] compteursParCluster = new int[resultat.nombreClusters];

        // Calculer la silhouette pour chaque point
        for (int i = 0; i < resultat.affectations.length; i++) {
            int cluster = resultat.affectations[i];
            if (cluster >= 0) {
                double silhouette = calculerSilhouettePoint(i, resultat, metrique);
                scoresParCluster[cluster] += silhouette;
                compteursParCluster[cluster]++;
            }
        }

        // Moyenner par cluster
        for (int i = 0; i < resultat.nombreClusters; i++) {
            if (compteursParCluster[i] > 0) {
                scoresParCluster[i] /= compteursParCluster[i];
            }
        }

        return scoresParCluster;
    }

    /**
     * Teste différents paramètres DBSCAN et retourne les scores de silhouette.
     */
    public void testerParametresDBSCAN(BufferedImage image,
                                       double[] epsValues,
                                       int[] minPtsValues,
                                       MetriqueDistance<PixelData> metrique,
                                       clustering.ClusteringManager manager) {
        System.out.println("=== Test des paramètres DBSCAN ===");
        System.out.println("eps\tminPts\tClusters\tSilhouette");

        for (double eps : epsValues) {
            for (int minPts : minPtsValues) {
                // Exécuter DBSCAN
                ResultatClustering resultat = manager.clusteriserImage(
                        image,
                        clustering.ClusteringManager.Algorithmes.dbscan(eps, minPts),
                        clustering.ClusteringManager.TypeClustering.BIOMES_EUCLIDIENNE
                );

                // Calculer le score de silhouette
                double score = calculer(resultat, metrique);

                System.out.printf("%.1f\t%d\t%d\t%.3f\n",
                        eps, minPts, resultat.nombreClusters, score);
            }
        }
    }
}