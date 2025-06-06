import clustering.ClusteringManager;
import clustering.ClusteringManager.*;
import clustering.algorithmes.AlgorithmeClustering;
import filtres.*;
import outils.OutilsImage;
import outils.PixelData;
import validation.DaviesBouldinIndex;
import validation.SilhouetteScore;
import visualisation.VisualisationBiomes;
import visualisation.VisualisationEcosystemes;
import metriques.couleur.MetriqueCouleur;
import metriques.position.MetriquePositionEuclidienne;
import normeCouleurs.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Programme console pour l'analyse complète d'exoplanètes
 */
public class MainConsole {

    // Gestionnaires
    private static final ClusteringManager manager = new ClusteringManager();
    private static final VisualisationBiomes visuBiomes = new VisualisationBiomes();
    private static final VisualisationEcosystemes visuEcosystemes = new VisualisationEcosystemes();
    private static final DaviesBouldinIndex daviesBouldin = new DaviesBouldinIndex();
    private static final SilhouetteScore silhouetteScore = new SilhouetteScore();

    // Scanner pour les entrées utilisateur
    private static final Scanner scanner = new Scanner(System.in);

    // Variables globales pour stocker l'état
    private static BufferedImage imageOriginale;
    private static BufferedImage imageFiltree;
    private static ResultatClustering resultatBiomes;
    private static List<ResultatClustering> resultatsEcosystemes = new ArrayList<>();
    private static String[] etiquettesBiomes;
    private static String nomPlanete;

    public static void main(String[] args) {
        System.out.println("=== ANALYSEUR D'EXOPLANÈTES - VERSION CONSOLE ===\n");

        boolean continuer = true;

        while (continuer) {
            afficherMenuPrincipal();
            int choix = lireEntier("Votre choix: ", 0, 6);

            switch (choix) {
                case 1:
                    chargerImage();
                    break;
                case 2:
                    appliquerFiltre();
                    break;
                case 3:
                    detecterBiomes();
                    break;
                case 4:
                    detecterEcosystemes();
                    break;
                case 5:
                    exporterResultats();
                    break;
                case 6:
                    analyseComplete();
                    break;
                case 0:
                    continuer = false;
                    System.out.println("\nAu revoir!");
                    break;
            }

            if (continuer) {
                System.out.println("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    /**
     * Affiche le menu principal
     */
    private static void afficherMenuPrincipal() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Charger une image d'exoplanète");
        System.out.println("2. Appliquer un filtre");
        System.out.println("3. Détecter les biomes");
        System.out.println("4. Détecter les écosystèmes");
        System.out.println("5. Exporter les résultats");
        System.out.println("6. Analyse complète automatique");
        System.out.println("0. Quitter");
    }

    /**
     * 1. Charger une image
     */
    private static void chargerImage() {
        System.out.println("\n=== CHARGEMENT D'IMAGE ===");

        // Lister les images disponibles
        File dossier = new File("./exoplanètes");
        File[] images = dossier.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png"));

        if (images == null || images.length == 0) {
            System.out.println("Aucune image trouvée dans ./exoplanètes");
            return;
        }

        System.out.println("Images disponibles:");
        for (int i = 0; i < images.length; i++) {
            System.out.println((i + 1) + ". " + images[i].getName());
        }

        int choix = lireEntier("Choisissez une image: ", 1, images.length);
        File imageChoisie = images[choix - 1];

        try {
            imageOriginale = OutilsImage.convertionCheminEnBufferedImage(imageChoisie.getAbsolutePath());
            nomPlanete = imageChoisie.getName().replaceAll("\\.[^.]+$", "");

            System.out.println("\nImage chargée avec succès!");
            System.out.println("Nom: " + imageChoisie.getName());
            System.out.println("Dimensions: " + imageOriginale.getWidth() + "x" + imageOriginale.getHeight());

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement: " + e.getMessage());
        }
    }

    /**
     * 2. Appliquer un filtre
     */
    private static void appliquerFiltre() {
        if (imageOriginale == null) {
            System.out.println("\nVeuillez d'abord charger une image!");
            return;
        }

        System.out.println("\n=== APPLICATION DE FILTRE ===");
        System.out.println("1. Flou Gaussien");
        System.out.println("2. Flou Moyenne");

        int typeFiltre = lireEntier("Type de filtre: ", 1, 2);
        int taille = lireEntier("Taille du filtre (3-15, impair): ", 3, 15);

        // Vérifier que la taille est impaire
        if (taille % 2 == 0) {
            taille++;
            System.out.println("Taille ajustée à " + taille + " (doit être impaire)");
        }

        try {
            Filtre filtre;

            if (typeFiltre == 1) {
                double sigma = lireDouble("Sigma (0.5-5.0): ", 0.5, 5.0);
                filtre = new FiltreFlouGaussien(taille, sigma);
            } else {
                filtre = new FiltreFlouMoyenne(taille);
            }

            // Créer le dossier temporaire
            Files.createDirectories(Paths.get("./temp"));

            String cheminTemp = "./temp/" + nomPlanete + "_filtree.jpg";

            System.out.println("Application du filtre en cours...");
            long debut = System.currentTimeMillis();

            filtre.appliquerFiltre("./exoplanètes/" + nomPlanete + ".jpg", cheminTemp);
            imageFiltree = OutilsImage.convertionCheminEnBufferedImage(cheminTemp);

            long duree = System.currentTimeMillis() - debut;

            System.out.println("Filtre appliqué avec succès!");
            System.out.println("Type: " + filtre.getNomFiltre());
            System.out.println("Temps d'exécution: " + duree + " ms");

        } catch (IOException e) {
            System.err.println("Erreur lors de l'application du filtre: " + e.getMessage());
        }
    }

    /**
     * 3. Détecter les biomes
     */
    private static void detecterBiomes() {
        if (imageFiltree == null) {
            System.out.println("\nVeuillez d'abord appliquer un filtre!");
            return;
        }

        System.out.println("\n=== DÉTECTION DES BIOMES ===");

        // Choix de l'algorithme
        System.out.println("Algorithme:");
        System.out.println("1. K-Means (recommandé pour les biomes)");
        System.out.println("2. DBSCAN");
        int algoChoice = lireEntier("Votre choix: ", 1, 2);

        // Choix de la métrique
        System.out.println("\nMétrique de couleur:");
        System.out.println("1. CIELAB");
        System.out.println("2. CIE94 (recommandé)");
        System.out.println("3. Euclidienne");
        System.out.println("4. Redmean");
        int metricChoice = lireEntier("Votre choix: ", 1, 4);

        // Paramètres selon l'algorithme
        AlgorithmeClustering<PixelData> algorithme;
        if (algoChoice == 1) {
            int k = lireEntier("Nombre de biomes (2-15): ", 2, 15);
            algorithme = Algorithmes.kmeans(k);
        } else {
            double eps = lireDouble("Epsilon (5-100): ", 5.0, 100.0);
            int minPts = lireEntier("MinPts (10-200): ", 10, 200);
            algorithme = Algorithmes.dbscan(eps, minPts);
        }

        // Choisir le type de clustering selon la métrique
        TypeClustering type;
        switch (metricChoice) {
            case 1: type = TypeClustering.BIOMES_CIELAB; break;
            case 2: type = TypeClustering.BIOMES_CIE94; break;
            case 3: type = TypeClustering.BIOMES_EUCLIDIENNE; break;
            default: type = TypeClustering.BIOMES_REDMEAN; break;
        }

        System.out.println("\nDétection en cours...");
        long debut = System.currentTimeMillis();

        resultatBiomes = manager.clusteriserImage(imageFiltree, algorithme, type);
        etiquettesBiomes = visuBiomes.etiquerBiomes(resultatBiomes);

        long duree = System.currentTimeMillis() - debut;

        // Afficher les résultats
        System.out.println("\n=== RÉSULTATS ===");
        System.out.println("Biomes détectés: " + resultatBiomes.nombreClusters);
        System.out.println("Temps d'exécution: " + duree + " ms");

        // Calculer les indices de validation
        NormeCouleurs norme = metricChoice == 1 ? new NormeCielab() :
                metricChoice == 2 ? new NormeCie94() :
                        metricChoice == 3 ? new NormeEuclidienne() : new NormeRedmean();
        MetriqueCouleur metrique = new MetriqueCouleur(norme);

        if (algoChoice == 1) { // K-Means
            double dbIndex = daviesBouldin.calculer(resultatBiomes, metrique);
            System.out.println("Indice Davies-Bouldin: " + String.format("%.3f", dbIndex));
            System.out.println("  (Plus faible = meilleur, < 1.0 = bon clustering)");
        }

        double silhouette = silhouetteScore.calculer(resultatBiomes, metrique);
        System.out.println("Score de Silhouette: " + String.format("%.3f", silhouette));
        System.out.println("  (Plus proche de 1 = meilleur)");

        // Lister les biomes
        System.out.println("\nBiomes détectés:");
        for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
            int nbPixels = resultatBiomes.getPixelsCluster(i).length;
            double pourcentage = (nbPixels * 100.0) / resultatBiomes.pixels.length;
            System.out.printf("  %d. %s - %d pixels (%.1f%%)\n",
                    i, etiquettesBiomes[i], nbPixels, pourcentage);
        }
    }

    /**
     * 4. Détecter les écosystèmes
     */
    private static void detecterEcosystemes() {
        if (resultatBiomes == null) {
            System.out.println("\nVeuillez d'abord détecter les biomes!");
            return;
        }

        System.out.println("\n=== DÉTECTION DES ÉCOSYSTÈMES ===");
        System.out.println("1. Analyser un biome spécifique");
        System.out.println("2. Analyser tous les biomes");

        int choix = lireEntier("Votre choix: ", 1, 2);

        // Choix de l'algorithme
        System.out.println("\nAlgorithme:");
        System.out.println("1. DBSCAN Optimisé (recommandé)");
        System.out.println("2. K-Means");
        System.out.println("3. DBSCAN Standard");
        int algoChoice = lireEntier("Votre choix: ", 1, 3);

        // Paramètres
        AlgorithmeClustering<PixelData> algorithme;
        if (algoChoice == 2) {
            int k = lireEntier("Nombre d'écosystèmes par biome (2-10): ", 2, 10);
            algorithme = Algorithmes.kmeans(k);
        } else {
            double eps = lireDouble("Epsilon (distance en pixels, 10-200): ", 10.0, 200.0);
            int minPts = lireEntier("MinPts (5-100): ", 5, 100);
            algorithme = algoChoice == 1 ?
                    Algorithmes.dbscanOpti(eps, minPts) :
                    Algorithmes.dbscan(eps, minPts);
        }

        // Réinitialiser la liste si nécessaire
        if (resultatsEcosystemes.size() < resultatBiomes.nombreClusters) {
            resultatsEcosystemes.clear();
            for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
                resultatsEcosystemes.add(null);
            }
        }

        if (choix == 1) {
            // Analyser un biome spécifique
            System.out.println("\nBiomes disponibles:");
            for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
                System.out.println(i + ". " + etiquettesBiomes[i]);
            }

            int biomeId = lireEntier("Biome à analyser: ", 0, resultatBiomes.nombreClusters - 1);
            analyserBiome(biomeId, algorithme);

        } else {
            // Analyser tous les biomes
            System.out.println("\nAnalyse de tous les biomes...");
            int totalEco = 0;

            for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
                PixelData[] pixels = resultatBiomes.getPixelsCluster(i);
                if (pixels.length < 100) {
                    System.out.println("Biome " + i + " (" + etiquettesBiomes[i] + ") trop petit, ignoré");
                    continue;
                }

                int nbEco = analyserBiome(i, algorithme);
                totalEco += nbEco;
            }

            System.out.println("\nTotal écosystèmes détectés: " + totalEco);
        }
    }

    /**
     * Analyser un biome spécifique
     */
    private static int analyserBiome(int biomeId, AlgorithmeClustering<PixelData> algorithme) {
        PixelData[] pixels = resultatBiomes.getPixelsCluster(biomeId);
        System.out.println("\nAnalyse du biome " + biomeId + " (" + etiquettesBiomes[biomeId] + ")...");
        System.out.println("Nombre de pixels: " + pixels.length);

        if (pixels.length < 100) {
            System.out.println("Biome trop petit pour l'analyse!");
            return 0;
        }

        long debut = System.currentTimeMillis();
        ResultatClustering eco = manager.clusteriserSousEnsemble(
                pixels, algorithme, TypeClustering.ECOSYSTEMES_POSITION);
        long duree = System.currentTimeMillis() - debut;

        resultatsEcosystemes.set(biomeId, eco);

        System.out.println("Écosystèmes détectés: " + eco.nombreClusters);
        System.out.println("Temps: " + duree + " ms");

        // Score de silhouette
        MetriquePositionEuclidienne metriquePos = new MetriquePositionEuclidienne();
        double silhouette = silhouetteScore.calculer(eco, metriquePos);
        System.out.println("Score de Silhouette: " + String.format("%.3f", silhouette));

        return eco.nombreClusters;
    }

    /**
     * 5. Exporter les résultats
     */
    private static void exporterResultats() {
        if (resultatBiomes == null) {
            System.out.println("\nAucun résultat à exporter!");
            return;
        }

        System.out.println("\n=== EXPORT DES RÉSULTATS ===");
        System.out.println("Dossier de destination (défaut: ./resultats): ");
        String dossier = scanner.nextLine().trim();
        if (dossier.isEmpty()) {
            dossier = "./resultats";
        }

        try {
            String dossierExport = dossier + "/" + nomPlanete;

            System.out.println("\nExport en cours...");

            // Exporter les biomes
            System.out.println("- Export des biomes...");
            visuBiomes.sauvegarderTousBiomes(imageFiltree, resultatBiomes,
                    dossierExport, nomPlanete);

            // Exporter les écosystèmes si disponibles
            boolean hasEco = false;
            for (ResultatClustering eco : resultatsEcosystemes) {
                if (eco != null) {
                    hasEco = true;
                    break;
                }
            }

            if (hasEco) {
                System.out.println("- Export des écosystèmes...");
                String dossierEco = dossierExport + "/ecosystemes";
                Files.createDirectories(Paths.get(dossierEco));

                for (int i = 0; i < resultatsEcosystemes.size(); i++) {
                    if (resultatsEcosystemes.get(i) != null) {
                        PixelData[] pixels = resultatBiomes.getPixelsCluster(i);
                        visuEcosystemes.sauvegarderEcosystemesBiome(
                                imageFiltree, pixels, resultatsEcosystemes.get(i),
                                dossierEco, etiquettesBiomes[i], i);
                    }
                }
            }

            System.out.println("\nExport terminé avec succès!");
            System.out.println("Résultats sauvegardés dans: " + dossierExport);

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export: " + e.getMessage());
        }
    }

    /**
     * 6. Analyse complète automatique
     */
    private static void analyseComplete() {
        System.out.println("\n=== ANALYSE COMPLÈTE AUTOMATIQUE ===");
        System.out.println("Cette option va:");
        System.out.println("1. Charger une image");
        System.out.println("2. Appliquer un filtre gaussien 5x5");
        System.out.println("3. Détecter les biomes avec K-Means et CIE94");
        System.out.println("4. Détecter les écosystèmes avec DBSCAN (Celui optimisé)");
        System.out.println("5. Exporter tous les résultats");

        System.out.print("\nContinuer? (o/n): ");
        if (!scanner.nextLine().toLowerCase().startsWith("o")) {
            return;
        }

        // 1. Charger l'image
        chargerImage();
        if (imageOriginale == null) return;

        // 2. Appliquer le filtre
        try {
            System.out.println("\nApplication du filtre gaussien 5x5...");
            Filtre filtre = new FiltreFlouGaussien(5, 1.5);
            Files.createDirectories(Paths.get("./temp"));
            String cheminTemp = "./temp/" + nomPlanete + "_filtree.jpg";
            filtre.appliquerFiltre("./exoplanètes/" + nomPlanete + ".jpg", cheminTemp);
            imageFiltree = OutilsImage.convertionCheminEnBufferedImage(cheminTemp);
            System.out.println("Filtre appliqué!");
        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
            return;
        }

        // 3. Détecter les biomes
        int nbBiomes = lireEntier("Nombre de biomes à détecter (5-8 recommandé): ", 2, 15);
        System.out.println("\nDétection des biomes avec CIE94...");
        resultatBiomes = manager.clusteriserImage(
                imageFiltree,
                Algorithmes.kmeans(nbBiomes),
                TypeClustering.BIOMES_CIE94
        );
        etiquettesBiomes = visuBiomes.etiquerBiomes(resultatBiomes);
        System.out.println("Biomes détectés: " + resultatBiomes.nombreClusters);

        // 4. Détecter les écosystèmes
        System.out.println("\nDétection des écosystèmes...");
        resultatsEcosystemes.clear();
        for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
            resultatsEcosystemes.add(null);
        }

        int totalEco = 0;
        for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
            PixelData[] pixels = resultatBiomes.getPixelsCluster(i);
            if (pixels.length >= 100) {
                ResultatClustering eco = manager.clusteriserSousEnsemble(
                        pixels,
                        Algorithmes.dbscanOpti(50.0, 30),
                        TypeClustering.ECOSYSTEMES_POSITION
                );
                resultatsEcosystemes.set(i, eco);
                totalEco += eco.nombreClusters;
                System.out.println("Biome " + i + ": " + eco.nombreClusters + " écosystèmes");
            }
        }
        System.out.println("Total écosystèmes: " + totalEco);

        // 5. Exporter
        System.out.println("\nExport des résultats...");
        exporterResultats();

        System.out.println("\n=== ANALYSE COMPLÈTE TERMINÉE ===");
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Lit un entier dans une plage donnée
     */
    private static int lireEntier(String message, int min, int max) {
        while (true) {
            System.out.print(message);
            try {
                String ligne = scanner.nextLine();
                int valeur = Integer.parseInt(ligne);
                if (valeur >= min && valeur <= max) {
                    return valeur;
                }
                System.out.println("Valeur hors limites! (" + min + "-" + max + ")");
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre entier!");
            }
        }
    }

    /**
     * Lit un double dans une plage donnée
     */
    private static double lireDouble(String message, double min, double max) {
        while (true) {
            System.out.print(message);
            try {
                String ligne = scanner.nextLine();
                double valeur = Double.parseDouble(ligne);
                if (valeur >= min && valeur <= max) {
                    return valeur;
                }
                System.out.println("Valeur hors limites! (" + min + "-" + max + ")");
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre décimal!");
            }
        }
    }
}