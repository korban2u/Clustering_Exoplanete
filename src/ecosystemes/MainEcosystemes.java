package ecosystemes;

import biomes.EtiqueteurBiomes;
import clusters.KMeans;
import biomes.VisualisationBiomes;
import normeCouleurs.NormeCie94;
import normeCouleurs.NormeCouleurs;
import outils.OutilsImage;
import outils.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe principale pour la détection complète des biomes et écosystèmes
 */
public class MainEcosystemes {

    // Définition des biomes - même structure que dans EtiqueteurBiomes
    private static final Map<Color, String> COULEURS_BIOMES = new HashMap<>();

    static {
        // Nouvelle palette de couleurs - identique à EtiqueteurBiomes
        COULEURS_BIOMES.put(new Color(220, 220, 200), "Tundra");           // #DCDCC8
        COULEURS_BIOMES.put(new Color(45, 70, 45), "Taiga");               // #2D462D
        COULEURS_BIOMES.put(new Color(80, 120, 50), "Foret temperee");     // #507832
        COULEURS_BIOMES.put(new Color(30, 80, 30), "Foret tropicale");     // #1E501E
        COULEURS_BIOMES.put(new Color(180, 160, 80), "Savane");            // #B4A050
        COULEURS_BIOMES.put(new Color(120, 160, 80), "Prairie");           // #78A050
        COULEURS_BIOMES.put(new Color(210, 180, 140), "Desert");           // #D2B48C
        COULEURS_BIOMES.put(new Color(240, 245, 250), "Glacier");          // #F0F5FA
        COULEURS_BIOMES.put(new Color(100, 180, 200), "Eau peu profonde"); // #64B4C8
        COULEURS_BIOMES.put(new Color(20, 50, 80), "Eau profonde");        // #143250
    }

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
            // Paramètres pour K-Means - adapter selon le nombre de biomes
            NormeCouleurs normeCie94 = new NormeCie94();
            KMeans kmeans = new KMeans(normeCie94, COULEURS_BIOMES.size());
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
                    image, affectationsBiomes, COULEURS_BIOMES.size(), eps, minPts);

            // 7. Étiqueter les biomes en utilisant EtiqueteurBiomes
            String[] etiquettesBiomes = EtiqueteurBiomes.etiqueterTousLesBiomes(kmeans.getCentroides(), normeCie94);

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