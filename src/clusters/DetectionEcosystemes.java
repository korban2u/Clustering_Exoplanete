package clusters;

import outils.OutilsImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Classe pour détecter et visualiser les écosystèmes dans chaque biome
 */
public class DetectionEcosystemes {

    // Couleurs prédéfinies pour différencier les écosystèmes
    private static final Color[] COULEURS_ECOSYSTEMES = {
            new Color(255, 0, 0),      // Rouge
            new Color(0, 255, 0),      // Vert
            new Color(0, 0, 255),      // Bleu
            new Color(255, 255, 0),    // Jaune
            new Color(255, 0, 255),    // Magenta
            new Color(0, 255, 255),    // Cyan
            new Color(255, 128, 0),    // Orange
            new Color(128, 0, 255),    // Violet
            new Color(0, 128, 255),    // Bleu clair
            new Color(255, 0, 128),    // Rose
            new Color(128, 255, 0),    // Vert clair
            new Color(0, 255, 128),    // Turquoise
            new Color(255, 128, 128),  // Rose pâle
            new Color(128, 255, 128),  // Vert pâle
            new Color(128, 128, 255)   // Bleu pâle
    };

    /**
     * Détecte les écosystèmes pour chaque biome dans l'image
     * @param imageOriginale Image d'origine
     * @param affectationsBiomes Affectations des biomes pour chaque pixel
     * @param nombreBiomes Nombre total de biomes
     * @param eps Paramètre epsilon pour DBSCAN
     * @param minPts Paramètre minPts pour DBSCAN
     * @return Map associant chaque biome à ses écosystèmes
     */
    public static Map<Integer, int[]> detecterEcosystemes(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            int nombreBiomes,
            double eps,
            int minPts) {

        Map<Integer, int[]> ecosystemesParBiome = new HashMap<>();
        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();

        List<Thread> threads = new ArrayList<>();
        // Pour chaque biome
        for (int biome = 0; biome < nombreBiomes; biome++) {
            int finalBiome = biome;

            Thread thread = new Thread(() ->{
                // Collecter les positions des pixels de ce biome
                List<int[]> positionsPixelsBiome = new ArrayList<>();
                Map<Integer, Integer> indexToPixelIndex = new HashMap<>();

                int pixelIndex = 0;
                int biomePixelIndex = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (affectationsBiomes[pixelIndex] == finalBiome) {
                            positionsPixelsBiome.add(new int[]{x, y});
                            indexToPixelIndex.put(biomePixelIndex, pixelIndex);
                            biomePixelIndex++;
                        }
                        pixelIndex++;
                    }
                }


                // Convertir la liste en tableau pour DBSCAN
                int[][] positions = positionsPixelsBiome.toArray(new int[0][]);

                // Appliquer DBSCAN sur les positions
                DBSCAN dbscan = new DBSCAN(eps, minPts);
                int[] ecosystemes = dbscan.classifier(positions);

                // Mapper les résultats aux indices originaux
                int[] ecosystemesComplets = new int[width * height];
                Arrays.fill(ecosystemesComplets, -1);

                for (int i = 0; i < ecosystemes.length; i++) {
                    int originalIndex = indexToPixelIndex.get(i);
                    ecosystemesComplets[originalIndex] = ecosystemes[i];
                }

                ecosystemesParBiome.put(finalBiome, ecosystemesComplets);

                System.out.println("Biome " + finalBiome + " : " +
                        dbscan.getNombreClusters() + " écosystèmes détectés");
            });
            threads.add(thread);
            thread.start();



        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ecosystemesParBiome;
    }

    /**
     * Visualise les écosystèmes d'un biome spécifique
     */
    public static BufferedImage visualiserEcosystemesBiome(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            int[] ecosystemes,
            int numeroBiome,
            String nomBiome) {

        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();

        // Créer le fond clair (75% plus lumineux)
        BufferedImage resultat = creerFondClair(imageOriginale, 75);

        // Remplacer les pixels du biome par les couleurs d'écosystèmes
        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (affectationsBiomes[pixelIndex] == numeroBiome &&
                        ecosystemes[pixelIndex] >= 0) {

                    int ecosysteme = ecosystemes[pixelIndex];
                    Color couleur = COULEURS_ECOSYSTEMES[ecosysteme % COULEURS_ECOSYSTEMES.length];
                    resultat.setRGB(x, y, couleur.getRGB());
                }
                pixelIndex++;
            }
        }

        // Ajouter le titre
        Graphics2D g2d = resultat.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(nomBiome, 20, 40);
        g2d.dispose();

        return resultat;
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

                int r = augmenterCanal(c.getRed(), pourcentage);
                int g = augmenterCanal(c.getGreen(), pourcentage);
                int b = augmenterCanal(c.getBlue(), pourcentage);

                fondClair.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return fondClair;
    }

    /**
     * Augmente la valeur d'un canal de couleur
     */
    private static int augmenterCanal(int valeur, int pourcentage) {
        return Math.round(valeur + (pourcentage / 100.0f) * (255 - valeur));
    }

    /**
     * Sauvegarde toutes les visualisations d'écosystèmes
     */
    public static void sauvegarderToutesVisualisations(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            Map<Integer, int[]> ecosystemesParBiome,
            String[] nomsBiomes,
            String dossierDestination) throws IOException {

        for (Map.Entry<Integer, int[]> entry : ecosystemesParBiome.entrySet()) {
            int biome = entry.getKey();
            int[] ecosystemes = entry.getValue();

            String nomBiome = (biome < nomsBiomes.length) ?
                    nomsBiomes[biome] : "Biome " + biome;

            BufferedImage visualisation = visualiserEcosystemesBiome(
                    imageOriginale, affectationsBiomes, ecosystemes, biome, nomBiome);

            String chemin = dossierDestination + "/ecosystemes_" +
                    nomBiome.toLowerCase().replace(" ", "_") + ".jpg";

            OutilsImage.sauverImage(visualisation, chemin);
        }
    }
}