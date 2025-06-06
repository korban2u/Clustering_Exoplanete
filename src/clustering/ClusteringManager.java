package clustering;

import clustering.algorithmes.*;
import metriques.MetriqueDistance;
import metriques.couleur.MetriqueCouleur;
import metriques.position.MetriquePositionEuclidienne;
import normeCouleurs.*;
import outils.PixelData;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.*;

/**
 * Gestionnaire principal pour effectuer le clustering sur des images.
 * Fournit une interface simple pour utiliser n'importe quel algorithme
 * avec n'importe quelle métrique.
 */
public class ClusteringManager {


    /**
     * Effectue un clustering sur une image selon le type de données voulu.
     */
    public ResultatClustering clusteriserImage(BufferedImage image, AlgorithmeClustering<PixelData> algorithme, TypeClustering type) {
        // Extraire les données de l'image
        PixelData[] pixels = extrairePixels(image);

        // Choisir la métrique appropriée
        MetriqueDistance<PixelData> metrique = obtenirMetrique(type);


        // Exécuter le clustering
        long debut = System.currentTimeMillis();
        int[] affectations = algorithme.executer(pixels, metrique);
        long duree = System.currentTimeMillis() - debut;

        // Construire et retourner le résultat
        return new ResultatClustering(affectations, algorithme.getNombreClusters(), algorithme.getNom(), metrique.getNom(), duree, pixels
        );
    }

    /**
     * Effectue un clustering sur un sous-ensemble de pixels (utile pour les écosystèmes).
     */
    public ResultatClustering clusteriserSousEnsemble(PixelData[] pixels, AlgorithmeClustering<PixelData> algorithme, TypeClustering type) {
        MetriqueDistance<PixelData> metrique = obtenirMetrique(type);

        long debut = System.currentTimeMillis();
        int[] affectations = algorithme.executer(pixels, metrique);
        long duree = System.currentTimeMillis() - debut;

        return new ResultatClustering(
                affectations,
                algorithme.getNombreClusters(),
                algorithme.getNom(),
                metrique.getNom(),
                duree,
                pixels
        );
    }

    /**
     * Extrait tous les pixels d'une image sous forme de PixelData.
     */
    private PixelData[] extrairePixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        PixelData[] pixels = new PixelData[width * height];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color couleur = new Color(image.getRGB(x, y));
                pixels[index] = new PixelData(x, y, couleur, index);
                index++;
            }
        }

        return pixels;
    }

    /**
     * Retourne la métrique appropriée selon le type de clustering.
     */
    private MetriqueDistance<PixelData> obtenirMetrique(TypeClustering type) {
        switch (type) {
            case BIOMES_EUCLIDIENNE:
                return new MetriqueCouleur(new NormeEuclidienne());

            case BIOMES_CIELAB:
                return new MetriqueCouleur(new NormeCielab());

            case BIOMES_CIE94:
                return new MetriqueCouleur(new NormeCie94());

            case BIOMES_REDMEAN:
                return new MetriqueCouleur(new NormeRedmean());

            case ECOSYSTEMES_POSITION:
                return new MetriquePositionEuclidienne();

            default:
                throw new IllegalArgumentException("Type de clustering non supporté: " + type);
        }
    }


    /**
     * Méthodes factory pour créer facilement des algorithmes.
     */
    public static class Algorithmes {
        public static AlgorithmeClustering<PixelData> kmeans(int k) {
            return new KMeans<>(k, 100);
        }

        public static AlgorithmeClustering<PixelData> dbscan(double eps, int minPts) {
            return new DBSCAN<>(eps, minPts);
        }

        public static AlgorithmeClustering<PixelData> dbscanOpti(double eps, int minPts) {
            return new DBSCANOptimise<>(eps, minPts);
        }

    }

    /**
     * Énumération des types de clustering disponibles.
     */
    public enum TypeClustering {
        BIOMES_EUCLIDIENNE,
        BIOMES_CIELAB,
        BIOMES_CIE94,
        BIOMES_REDMEAN,
        ECOSYSTEMES_POSITION
    }

    /**
     * Classe pour encapsuler les résultats du clustering.
     */
    public static class ResultatClustering {
        public final int[] affectations;
        public final int nombreClusters;
        public final String algorithme;
        public final String metrique;
        public final long dureeMs;
        public final PixelData[] pixels;


        public ResultatClustering(int[] affectations, int nombreClusters, String algorithme, String metrique, long dureeMs, PixelData[] pixels) {
            this.affectations = affectations;
            this.nombreClusters = nombreClusters;
            this.algorithme = algorithme;
            this.metrique = metrique;
            this.dureeMs = dureeMs;
            this.pixels = pixels;
        }

        /**
         * Extrait les pixels d'un cluster spécifique.
         */
        public PixelData[] getPixelsCluster(int cluster) {
            List<PixelData> pixelsCluster = new ArrayList<>();
            for (int i = 0; i < affectations.length; i++) {
                if (affectations[i] == cluster) {
                    pixelsCluster.add(pixels[i]);
                }
            }
            return pixelsCluster.toArray(new PixelData[0]);
        }

        /**
         * Calcule la couleur moyenne d'un cluster.
         */
        public Color getCouleurMoyenneCluster(int cluster) {
            int totalR = 0, totalG = 0, totalB = 0;
            int count = 0;

            for (int i = 0; i < affectations.length; i++) {
                if (affectations[i] == cluster) {
                    Color c = pixels[i].getCouleur();
                    totalR += c.getRed();
                    totalG += c.getGreen();
                    totalB += c.getBlue();
                    count++;
                }
            }

            if (count == 0) return Color.BLACK;

            return new Color(
                    totalR / count,
                    totalG / count,
                    totalB / count
            );
        }
    }
}