package validation;

import clustering.ClusteringManager.ResultatClustering;
import outils.PixelData;
import metriques.MetriqueDistance;
import java.util.*;

/**
 * Version simple et rapide du score de silhouette.
 * Utilise l'échantillonnage aléatoire pour accélérer le calcul.
 */
public class SilhouetteScore {

    private final Random random = new Random();

    /**
     * Calcule le score de silhouette sur un échantillon de points.
     * Beaucoup plus rapide que la version complète.
     */
    public double calculer(ResultatClustering resultat, MetriqueDistance<PixelData> metrique) {
        if (resultat.nombreClusters <= 1) return 0.0;

        // 1. Prendre un échantillon de 500 points max
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < resultat.affectations.length; i++) {
            if (resultat.affectations[i] >= 0) { // Ignorer le bruit
                indices.add(i);
            }
        }

        // Mélanger et prendre max 500 points
        Collections.shuffle(indices, random);
        int nbPoints = Math.min(500, indices.size());
        List<Integer> echantillon = indices.subList(0, nbPoints);

        // 2. Calculer le score sur l'échantillon
        double somme = 0.0;
        for (int idx : echantillon) {
            somme += calculerPourUnPoint(idx, resultat, metrique);
        }

        return somme / nbPoints;
    }

    /**
     * Calcule le score de silhouette pour un seul point.
     */
    private double calculerPourUnPoint(int index, ResultatClustering resultat,
                                       MetriqueDistance<PixelData> metrique) {
        int monCluster = resultat.affectations[index];
        PixelData monPoint = resultat.pixels[index];

        // Compter les distances
        double distanceIntra = 0.0;  // Distance moyenne dans mon cluster
        int nbIntra = 0;

        Map<Integer, Double> distancesAutresClusters = new HashMap<>();
        Map<Integer, Integer> compteursClusters = new HashMap<>();

        // Parcourir un échantillon des autres points (max 200)
        int step = Math.max(1, resultat.pixels.length / 200);

        for (int i = 0; i < resultat.pixels.length; i += step) {
            if (i == index) continue;

            int sonCluster = resultat.affectations[i];
            if (sonCluster < 0) continue; // Ignorer le bruit

            double distance = metrique.calculerDistance(monPoint, resultat.pixels[i]);

            if (sonCluster == monCluster) {
                // Même cluster
                distanceIntra += distance;
                nbIntra++;
            } else {
                // Autre cluster
                distancesAutresClusters.merge(sonCluster, distance, Double::sum);
                compteursClusters.merge(sonCluster, 1, Integer::sum);
            }
        }

        // Calculer a(i)
        double a = (nbIntra > 0) ? distanceIntra / nbIntra : 0.0;

        // Calculer b(i) : plus petite distance moyenne vers un autre cluster
        double b = Double.MAX_VALUE;
        for (Map.Entry<Integer, Double> entry : distancesAutresClusters.entrySet()) {
            int cluster = entry.getKey();
            double sommeDist = entry.getValue();
            int nbPoints = compteursClusters.get(cluster);

            double moyenneDist = sommeDist / nbPoints;
            if (moyenneDist < b) {
                b = moyenneDist;
            }
        }

        // Calculer le score
        if (b == Double.MAX_VALUE) return 0.0; // Pas d'autres clusters

        double max = Math.max(a, b);
        if (max == 0) return 0.0;

        return (b - a) / max;
    }
}