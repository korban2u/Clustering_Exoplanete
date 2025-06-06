package clustering;

import clustering.ClusteringManager.*;
import donnees.PixelData;
import outils.OutilsImage;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Exemple d'utilisation de la nouvelle architecture de clustering flexible.
 */
public class MainClustering {

    public static void main(String[] args) throws IOException {
        // Charger une image
        BufferedImage image = OutilsImage.convertionCheminEnBufferedImage("./exoplanetes/Planete 1.jpg");

        // Créer le gestionnaire
        ClusteringManager manager = new ClusteringManager();

        System.out.println("=== Détection de biomes ===\n");

        // Exemple 1: K-Means avec distance CIELAB pour les biomes
        System.out.println("1. K-Means avec CIELAB:");
        ResultatClustering biomesCIELAB = manager.clusteriserImage(
                image,
                Algorithmes.kmeans(7),  // 7 biomes
                TypeClustering.BIOMES_CIELAB
        );
        afficherResultat(biomesCIELAB);

        // Exemple 2: DBSCAN avec distance Euclidienne RGB pour les biomes
        System.out.println("\n2. DBSCAN avec Euclidienne RGB:");
        ResultatClustering biomesDBSCAN = manager.clusteriserImage(
                image,
                Algorithmes.dbscan(20.0, 50),  // eps=20, minPts=50
                TypeClustering.BIOMES_EUCLIDIENNE
        );
        afficherResultat(biomesDBSCAN);

        // Exemple 3: K-Means avec CIE94 pour les biomes
        System.out.println("\n3. K-Means avec CIE94:");
        ResultatClustering biomesCIE94 = manager.clusteriserImage(
                image,
                Algorithmes.kmeans(5),  // 5 biomes
                TypeClustering.BIOMES_CIE94
        );
        afficherResultat(biomesCIE94);

        System.out.println("\n=== Détection d'écosystèmes ===\n");

        // Pour chaque biome détecté, trouver les écosystèmes
        for (int biome = 0; biome < biomesCIELAB.nombreClusters; biome++) {
            // Extraire les pixels du biome
            PixelData[] pixelsBiome = biomesCIELAB.getPixelsCluster(biome);

            if (pixelsBiome.length < 100) continue; // Ignorer les petits biomes

            System.out.println("Biome " + biome + " (" + pixelsBiome.length + " pixels):");

            // Exemple 4: DBSCAN sur les positions pour détecter les écosystèmes
            ResultatClustering ecosystemes = manager.clusteriserSousEnsemble(
                    pixelsBiome,
                    Algorithmes.dbscan(50.0, 20),  // eps=50 pixels, minPts=20
                    TypeClustering.ECOSYSTEMES_POSITION
            );

            System.out.println("  - " + ecosystemes.nombreClusters + " écosystèmes détectés");
            System.out.println("  - Temps: " + ecosystemes.dureeMs + " ms");

            // On pourrait aussi utiliser K-Means sur les positions!
            ResultatClustering ecosystemesKMeans = manager.clusteriserSousEnsemble(
                    pixelsBiome,
                    Algorithmes.kmeans(3),  // 3 écosystèmes par biome
                    TypeClustering.ECOSYSTEMES_POSITION
            );

            System.out.println("  - K-Means: " + ecosystemesKMeans.nombreClusters + " écosystèmes");
        }

    }

    private static void afficherResultat(ResultatClustering resultat) {
        System.out.println("  Algorithme: " + resultat.algorithme);
        System.out.println("  Métrique: " + resultat.metrique);
        System.out.println("  Clusters trouvés: " + resultat.nombreClusters);
        System.out.println("  Temps d'exécution: " + resultat.dureeMs + " ms");
    }
}