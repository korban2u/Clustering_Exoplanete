package validation;

import clustering.ClusteringManager.ResultatClustering;
import outils.PixelData;
import metriques.MetriqueDistance;

import java.awt.image.BufferedImage;

/**
 * Implémentation de l'indice de Davies-Bouldin pour évaluer la qualité du clustering K-Means.
 * Plus la valeur est faible, meilleur est le clustering.
 */
public class DaviesBouldinIndex {

    /**
     * Calcule l'indice de Davies-Bouldin pour un résultat de clustering.
     *
     * @param resultat Le résultat du clustering
     * @param metrique La métrique de distance utilisée
     * @return La valeur de l'indice DB (plus faible = meilleur)
     */
    public double calculer(ResultatClustering resultat, MetriqueDistance metrique) {
        int K = resultat.nombreClusters;
        if (K <= 1) return 0.0; // Pas de sens pour 0 ou 1 cluster

        // Calculer les centroïdes de chaque cluster
        PixelData[] centroides = calculerCentroides(resultat);

        // Calculer les dispersions intra-cluster (Si)
        double[] dispersions = new double[K];
        for (int i = 0; i < K; i++) {
            dispersions[i] = calculerDispersionIntraCluster(resultat, i, centroides[i], metrique);
        }

        // Calculer l'indice DB
        double sommeDB = 0.0;

        for (int i = 0; i < K; i++) {
            double maxRatio = 0.0;

            // Trouver le maximum du ratio pour ce cluster
            for (int j = 0; j < K; j++) {
                if (i != j) {
                    // Distance entre les centroïdes i et j
                    double distanceCentroides = metrique.calculerDistance(centroides[i], centroides[j]);

                    if (distanceCentroides > 0) {
                        double ratio = (dispersions[i] + dispersions[j]) / distanceCentroides;
                        if (ratio > maxRatio) {
                            maxRatio = ratio;
                        }
                    }
                }
            }

            sommeDB += maxRatio;
        }

        return sommeDB / K;
    }

    /**
     * Calcule les centroïdes de chaque cluster.
     * Pour les pixels, on calcule la position et couleur moyennes.
     */
    private PixelData[] calculerCentroides(ResultatClustering resultat) {
        int K = resultat.nombreClusters;
        PixelData[] centroides = new PixelData[K];

        for (int cluster = 0; cluster < K; cluster++) {
            PixelData[] pixelsCluster = resultat.getPixelsCluster(cluster);

            if (pixelsCluster.length == 0) {
                // Cluster vide, utiliser un pixel par défaut
                centroides[cluster] = new PixelData(0, 0, java.awt.Color.BLACK, -1);
                continue;
            }

            // Calculer les moyennes
            double moyX = 0, moyY = 0;
            double moyR = 0, moyG = 0, moyB = 0;

            for (PixelData pixel : pixelsCluster) {
                moyX += pixel.getX();
                moyY += pixel.getY();
                moyR += pixel.getCouleur().getRed();
                moyG += pixel.getCouleur().getGreen();
                moyB += pixel.getCouleur().getBlue();
            }

            int n = pixelsCluster.length;
            int x = (int) Math.round(moyX / n);
            int y = (int) Math.round(moyY / n);
            int r = (int) Math.round(moyR / n);
            int g = (int) Math.round(moyG / n);
            int b = (int) Math.round(moyB / n);

            centroides[cluster] = new PixelData(x, y, new java.awt.Color(r, g, b), -1);
        }

        return centroides;
    }

    /**
     * Calcule la dispersion moyenne d'un cluster autour de son centroïde (Si).
     * Formule: Si = (1/|Ci|) * Σ ||x - μi||²
     */
    private double calculerDispersionIntraCluster(ResultatClustering resultat,
                                                  int cluster,
                                                  PixelData centroide,
                                                  MetriqueDistance metrique) {
        PixelData[] pixelsCluster = resultat.getPixelsCluster(cluster);

        if (pixelsCluster.length == 0) return 0.0;

        double sommeDistances = 0.0;

        for (PixelData pixel : pixelsCluster) {
            double distance = metrique.calculerDistance(pixel, centroide);
            sommeDistances += distance * distance; // Distance au carré
        }

        return Math.sqrt(sommeDistances / pixelsCluster.length);
    }

    /**
     * Teste différentes valeurs de K et retourne les scores DB.
     * Utile pour trouver le nombre optimal de clusters.
     */
    public double[] testerDifferentsK(BufferedImage image,
                                      int kMin, int kMax,
                                      MetriqueDistance metrique,
                                      clustering.ClusteringManager manager) {
        double[] scores = new double[kMax - kMin + 1];

        for (int k = kMin; k <= kMax; k++) {
            // Exécuter K-Means avec ce K
            ResultatClustering resultat = manager.clusteriserImage(
                    image,
                    clustering.ClusteringManager.Algorithmes.kmeans(k),
                    clustering.ClusteringManager.TypeClustering.BIOMES_EUCLIDIENNE
            );

            // Calculer le score DB
            scores[k - kMin] = calculer(resultat, metrique);

            System.out.println("K=" + k + " -> DB=" + scores[k - kMin]);
        }

        return scores;
    }
}