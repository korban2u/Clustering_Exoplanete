package ecosystemes;

import clusters.DBSCANOptimise;
import outils.OutilsImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Version optimisée de la détection d'écosystèmes qui exploite les informations de biomes
 */
public class DetectionEcosystemesOptimise {

    // Couleurs pour les écosystèmes
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
            new Color(0, 255, 128)     // Turquoise
    };

    /**
     * Structure pour stocker les informations d'un biome
     */
    private static class BiomeInfo {
        List<int[]> positions = new ArrayList<>();
        Map<Integer, Integer> indexToPixel = new HashMap<>();
        Map<Integer, Integer> pixelToIndex = new HashMap<>();
        int[] boundingBox = new int[4]; // minX, minY, maxX, maxY

        BiomeInfo() {
            boundingBox[0] = Integer.MAX_VALUE;
            boundingBox[1] = Integer.MAX_VALUE;
            boundingBox[2] = Integer.MIN_VALUE;
            boundingBox[3] = Integer.MIN_VALUE;
        }

        void ajouterPixel(int x, int y, int pixelIndex) {
            int biomeIndex = positions.size();
            positions.add(new int[]{x, y});
            indexToPixel.put(biomeIndex, pixelIndex);
            pixelToIndex.put(pixelIndex, biomeIndex);

            // Mettre à jour la bounding box
            boundingBox[0] = Math.min(boundingBox[0], x);
            boundingBox[1] = Math.min(boundingBox[1], y);
            boundingBox[2] = Math.max(boundingBox[2], x);
            boundingBox[3] = Math.max(boundingBox[3], y);
        }

        double getDiagonale() {
            int dx = boundingBox[2] - boundingBox[0];
            int dy = boundingBox[3] - boundingBox[1];
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    /**
     * Détecte les écosystèmes de manière optimisée en utilisant les informations de biomes
     */
    public static Map<Integer, int[]> detecterEcosystemesOptimise(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            int nombreBiomes,
            double epsBase,
            int minPtsBase) {

        System.out.println("=== Détection optimisée des écosystèmes ===");
        long debut = System.currentTimeMillis();

        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();

        // 1. Analyser et organiser les biomes
        Map<Integer, BiomeInfo> infoBiomes = analyserBiomes(
                affectationsBiomes, width, height, nombreBiomes);

        // 2. Traiter chaque biome en parallèle avec des paramètres adaptés
        Map<Integer, int[]> ecosystemesParBiome = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(nombreBiomes, Runtime.getRuntime().availableProcessors()));

        List<Future<Void>> futures = new ArrayList<>();

        for (int biome = 0; biome < nombreBiomes; biome++) {
            final int biomeId = biome;
            BiomeInfo info = infoBiomes.get(biome);

            if (info == null || info.positions.isEmpty()) {
                continue;
            }

            Future<Void> future = executor.submit(() -> {
                // Adapter les paramètres selon la taille du biome
                double eps = calculerEpsAdaptatif(info, epsBase);
                int minPts = calculerMinPtsAdaptatif(info, minPtsBase);

                System.out.printf("Biome %d : %d pixels, eps=%.1f, minPts=%d%n",
                        biomeId, info.positions.size(), eps, minPts);

                // Utiliser DBSCAN optimisé
                int[][] positions = info.positions.toArray(new int[0][]);
                DBSCANOptimise dbscan = new DBSCANOptimise(eps, minPts);
                int[] ecosystemes = dbscan.classifier(positions);

                // Mapper aux indices originaux
                int[] ecosystemesComplets = new int[width * height];
                Arrays.fill(ecosystemesComplets, -1);

                for (int i = 0; i < ecosystemes.length; i++) {
                    int originalIndex = info.indexToPixel.get(i);
                    ecosystemesComplets[originalIndex] = ecosystemes[i];
                }

                ecosystemesParBiome.put(biomeId, ecosystemesComplets);

                // Statistiques
                Set<Integer> uniqueEcosystemes = new HashSet<>();
                for (int eco : ecosystemes) {
                    if (eco >= 0) uniqueEcosystemes.add(eco);
                }

                System.out.printf("Biome %d : %d écosystèmes détectés%n",
                        biomeId, uniqueEcosystemes.size());

                return null;
            });

            futures.add(future);
        }

        // Attendre la fin de tous les traitements
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        long temps = System.currentTimeMillis() - debut;
        System.out.printf("Temps total de détection : %d ms%n", temps);

        return ecosystemesParBiome;
    }

    /**
     * Analyse les biomes et crée les structures d'information
     */
    private static Map<Integer, BiomeInfo> analyserBiomes(
            int[] affectationsBiomes, int width, int height, int nombreBiomes) {

        Map<Integer, BiomeInfo> infoBiomes = new HashMap<>();

        // Initialiser les infos pour chaque biome
        for (int i = 0; i < nombreBiomes; i++) {
            infoBiomes.put(i, new BiomeInfo());
        }

        // Parcourir l'image et organiser par biome
        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int biome = affectationsBiomes[pixelIndex];
                if (biome >= 0 && biome < nombreBiomes) {
                    infoBiomes.get(biome).ajouterPixel(x, y, pixelIndex);
                }
                pixelIndex++;
            }
        }

        return infoBiomes;
    }

    /**
     * Calcule un eps adaptatif basé sur la taille et la forme du biome
     */
    private static double calculerEpsAdaptatif(BiomeInfo info, double epsBase) {
        // Adapter eps selon la densité du biome
        double superficie = info.positions.size();
        double diagonale = info.getDiagonale();

        // Si le biome est très grand, augmenter eps
        if (superficie > 100000) {
            return epsBase * 1.5;
        }
        // Si le biome est très petit, réduire eps
        else if (superficie < 1000) {
            return epsBase * 0.5;
        }

        // Adapter selon la forme (compact vs étalé)
        double ratio = diagonale / Math.sqrt(superficie);
        if (ratio > 2.0) {
            // Biome étalé : augmenter eps
            return epsBase * 1.2;
        }

        return epsBase;
    }

    /**
     * Calcule un minPts adaptatif basé sur la densité du biome
     */
    private static int calculerMinPtsAdaptatif(BiomeInfo info, int minPtsBase) {
        double superficie = info.positions.size();

        // Adapter selon la taille
        if (superficie > 50000) {
            return (int)(minPtsBase * 1.5);
        } else if (superficie < 2000) {
            return Math.max(3, minPtsBase / 2);
        }

        return minPtsBase;
    }

    /**
     * Visualise les écosystèmes avec des statistiques
     */
    public static BufferedImage visualiserEcosystemesAvecStats(
            BufferedImage imageOriginale,
            int[] affectationsBiomes,
            int[] ecosystemes,
            int numeroBiome,
            String nomBiome) {

        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();

        // Créer le fond clair
        BufferedImage resultat = creerFondClair(imageOriginale, 75);

        // Compter les écosystèmes
        Map<Integer, Integer> compteurEcosystemes = new HashMap<>();
        Map<Integer, Color> couleursEcosystemes = new HashMap<>();

        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (affectationsBiomes[pixelIndex] == numeroBiome &&
                        ecosystemes[pixelIndex] >= 0) {

                    int eco = ecosystemes[pixelIndex];
                    compteurEcosystemes.merge(eco, 1, Integer::sum);

                    if (!couleursEcosystemes.containsKey(eco)) {
                        couleursEcosystemes.put(eco,
                                COULEURS_ECOSYSTEMES[eco % COULEURS_ECOSYSTEMES.length]);
                    }

                    resultat.setRGB(x, y, couleursEcosystemes.get(eco).getRGB());
                }
                pixelIndex++;
            }
        }

        // Ajouter titre et statistiques
        Graphics2D g2d = resultat.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Titre
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(nomBiome, 20, 40);

        // Statistiques
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString(String.format("%d écosystèmes détectés", compteurEcosystemes.size()), 20, 65);

        // Légende des principaux écosystèmes
        int yPos = 90;
        List<Map.Entry<Integer, Integer>> topEcosystemes = compteurEcosystemes.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();

        for (Map.Entry<Integer, Integer> entry : topEcosystemes) {
            Color couleur = couleursEcosystemes.get(entry.getKey());
            g2d.setColor(couleur);
            g2d.fillRect(20, yPos - 12, 20, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.format("Écosystème %d : %d pixels",
                    entry.getKey(), entry.getValue()), 45, yPos);
            yPos += 20;
        }

        g2d.dispose();
        return resultat;
    }

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

    /**
     * Sauvegarde toutes les visualisations avec la version optimisée
     */
    public static void sauvegarderToutesVisualisationsOptimisees(
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

            BufferedImage visualisation = visualiserEcosystemesAvecStats(
                    imageOriginale, affectationsBiomes, ecosystemes, biome, nomBiome);

            String chemin = dossierDestination + "/ecosystemes_optimise_" +
                    nomBiome.toLowerCase().replace(" ", "_") + ".jpg";

            OutilsImage.sauverImage(visualisation, chemin);
        }
    }
}