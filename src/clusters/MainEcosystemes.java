package clusters;

import normeCouleurs.NormeCie94;
import normeCouleurs.NormeCouleurs;
import normeCouleurs.NormeEuclidienne;
import outils.OutilsImage;
import outils.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * Classe principale pour la détection complète des biomes et écosystèmes
 */
public class MainEcosystemes {

    // Définition des biomes selon la Figure 6 du sujet
    private static final String[] NOMS_BIOMES = {
            "Eau profonde",
            "Eau peu profonde",
            "Déserts",
            "Savanes",
            "Forêts",
            "Montagnes",
            "Glaciers"
    };

    private static final Color[] COULEURS_BIOMES = {
            new Color(0, 0, 128),      // Eau profonde - Bleu foncé
            new Color(0, 150, 200),    // Eau peu profonde - Bleu clair
            new Color(238, 207, 161),  // Déserts - Beige
            new Color(177, 209, 110),  // Savanes - Vert clair
            new Color(0, 100, 0),      // Forêts - Vert foncé
            new Color(128, 128, 128),  // Montagnes - Gris
            new Color(255, 255, 255)   // Glaciers - Blanc
    };

    public static void main(String[] args) {
        try {
            // Configuration
            String cheminImage = "./resultatFiltre/Planet_1_FlouGauss7x7.jpg";
            String dossierResultats = "./resultats_ecosystemes";

            // Paramètres pour DBSCAN
            // Pour une image 1400x1400, adapter eps selon l'échelle
            double eps = 20.0;  // Rayon de voisinage
            int minPts = 100;   // Nombre minimum de points

            System.out.println("=== DÉTECTION DES BIOMES ET ÉCOSYSTÈMES ===");
            System.out.println("Image : " + cheminImage);
            System.out.println("Paramètres DBSCAN : eps=" + eps + ", minPts=" + minPts);

            // 1. Charger l'image
            BufferedImage image = OutilsImage.convertionCheminEnBufferedImage(cheminImage);
            System.out.println("Dimensions : " + image.getWidth() + "x" + image.getHeight());

            // 2. Extraire les données RGB
            int[][] donneesRGB = OutilsImage.extraireDonneesPixels(image);

            // 3. Clustering K-Means pour les biomes
            System.out.println("\nÉtape 1 : Détection des biomes avec K-Means...");
            NormeCouleurs normeCie94 = new NormeCie94();
            KMeans kmeans = new KMeans(new NormeEuclidienne(), NOMS_BIOMES.length);
            int[] affectationsBiomes = kmeans.classifier(donneesRGB);

            // 4. Créer et afficher la palette des biomes trouvés
            Palette paletteBiomes = kmeans.creerPaletteBiomes(kmeans.getCentroides());
            afficherPaletteBiomes(kmeans.getCentroides());

            // 5. Visualiser les biomes
            BufferedImage imageBiomes = VisualisationBiomes.visualiserBiomes(
                    image, affectationsBiomes, paletteBiomes);
            OutilsImage.sauverImage(imageBiomes, dossierResultats + "/biomes_detectes.jpg");
            System.out.println("Biomes sauvegardés dans : " + dossierResultats + "/biomes_detectes.jpg");

            // 6. Détecter les écosystèmes pour chaque biome
            System.out.println("\nÉtape 2 : Détection des écosystèmes avec DBSCAN...");
            Map<Integer, int[]> ecosystemesParBiome = DetectionEcosystemes.detecterEcosystemes(
                    image, affectationsBiomes, NOMS_BIOMES.length, eps, minPts);

            // 7. Étiqueter les biomes
            String[] etiquettesBiomes = etiquerterBiomes(kmeans.getCentroides());

            // 8. Sauvegarder toutes les visualisations
            System.out.println("\nÉtape 3 : Génération des visualisations...");
            DetectionEcosystemes.sauvegarderToutesVisualisations(
                    image, affectationsBiomes, ecosystemesParBiome,
                    etiquettesBiomes, dossierResultats);

            // 9. Afficher aussi chaque biome individuellement
            System.out.println("\nÉtape 4 : Génération des biomes individuels...");
            sauvegarderBiomesIndividuels(image, affectationsBiomes,
                    kmeans.getCentroides(), etiquettesBiomes, dossierResultats);

            System.out.println("\n=== TRAITEMENT TERMINÉ ===");
            System.out.println("Résultats sauvegardés dans : " + dossierResultats);

        } catch (IOException e) {
            System.err.println("Erreur lors du traitement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les couleurs des biomes détectés
     */
    private static void afficherPaletteBiomes(int[][] centroides) {
        System.out.println("\nBiomes détectés :");
        for (int i = 0; i < centroides.length; i++) {
            System.out.printf("Biome %d : RGB(%d, %d, %d)\n",
                    i, centroides[i][0], centroides[i][1], centroides[i][2]);
        }
    }

    /**
     * Étiquette les biomes en trouvant la correspondance la plus proche
     */
    private static String[] etiquerterBiomes(int[][] centroides) {
        String[] etiquettes = new String[centroides.length];
        boolean[] biomesUtilises = new boolean[NOMS_BIOMES.length];

        for (int i = 0; i < centroides.length; i++) {
            Color couleurCentroide = new Color(
                    centroides[i][0], centroides[i][1], centroides[i][2]);

            double distanceMin = Double.MAX_VALUE;
            int biomeProche = -1;

            // Trouver le biome prédéfini le plus proche
            for (int j = 0; j < COULEURS_BIOMES.length; j++) {
                if (!biomesUtilises[j]) {
                    double distance = distanceRGB(couleurCentroide, COULEURS_BIOMES[j]);
                    if (distance < distanceMin) {
                        distanceMin = distance;
                        biomeProche = j;
                    }
                }
            }

            if (biomeProche >= 0) {
                etiquettes[i] = NOMS_BIOMES[biomeProche];
                biomesUtilises[biomeProche] = true;
            } else {
                etiquettes[i] = "Biome " + i;
            }
        }

        return etiquettes;
    }

    /**
     * Calcule la distance euclidienne entre deux couleurs
     */
    private static double distanceRGB(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /**
     * Sauvegarde chaque biome individuellement
     */
    private static void sauvegarderBiomesIndividuels(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            int[][] centroides,
            String[] etiquettes,
            String dossier) throws IOException {

        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();

        for (int biome = 0; biome < centroides.length; biome++) {
            // Créer le fond clair
            BufferedImage resultat = creerFondClair(imageOriginale, 75);

            // Remplacer uniquement les pixels du biome actuel
            int pixelIndex = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (affectationsBiomes[pixelIndex] == biome) {
                        resultat.setRGB(x, y, imageOriginale.getRGB(x, y));
                    }
                    pixelIndex++;
                }
            }

            // Ajouter le titre
            Graphics2D g2d = resultat.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString(etiquettes[biome], 20, 40);
            g2d.dispose();

            // Sauvegarder
            String nomFichier = dossier + "/biome_" +
                    etiquettes[biome].toLowerCase().replace(" ", "_") + ".jpg";
            OutilsImage.sauverImage(resultat, nomFichier);
        }
    }

    /**
     * Crée un fond clair à partir de l'image originale
     */
    private static BufferedImage creerFondClair(BufferedImage image, int pourcentage) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage fondClair = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));

                int r = Math.round(c.getRed() + (pourcentage / 100.0f) * (255 - c.getRed()));
                int g = Math.round(c.getGreen() + (pourcentage / 100.0f) * (255 - c.getGreen()));
                int b = Math.round(c.getBlue() + (pourcentage / 100.0f) * (255 - c.getBlue()));

                fondClair.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return fondClair;
    }
}