import clustering.ClusteringManager;
import clustering.ClusteringManager.*;
import donnees.PixelData;
import filtres.FiltreFlouGaussien;
import metriques.couleur.MetriqueCouleur;
import metriques.position.MetriquePositionEuclidienne;
import normeCouleurs.NormeCielab;
import outils.OutilsImage;
import validation.DaviesBouldinIndex;
import validation.SilhouetteScore;
import visualisation.VisualisationBiomes;
import visualisation.VisualisationEcosystemes;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Programme principal pour l'analyse complète des exoplanètes.
 * Effectue la détection des biomes et écosystèmes avec validation.
 */
public class Analyseur {

    private final ClusteringManager manager;
    private final VisualisationBiomes visuBiomes;
    private final VisualisationEcosystemes visuEcosystemes;
    private final DaviesBouldinIndex daviesBouldin;
    private final SilhouetteScore silhouetteScore;

    public Analyseur() {
        this.manager = new ClusteringManager();
        this.visuBiomes = new VisualisationBiomes();
        this.visuEcosystemes = new VisualisationEcosystemes();
        this.daviesBouldin = new DaviesBouldinIndex();
        this.silhouetteScore = new SilhouetteScore();
    }

    /**
     * Effectue l'analyse complète d'une image d'exoplanète.
     */
    public void analyserExoplanete(String cheminImage, String nomPlanete, int nombreBiomes) throws IOException {
        System.out.println("=== ANALYSE DE " + nomPlanete + " ===\n");

        // 1. Appliquer le filtre flou
        System.out.println("1. Application du filtre flou gaussien...");
        String cheminImageFiltree = appliquerFiltre(cheminImage, nomPlanete);

        // 2. Charger l'image filtrée
        BufferedImage image = OutilsImage.convertionCheminEnBufferedImage(cheminImageFiltree);

        // 3. Détecter les biomes
        System.out.println("\n2. Détection des biomes...");
        ResultatClustering biomes = detecterBiomes(image, nombreBiomes);

        // 4. Sauvegarder les résultats des biomes
        System.out.println("\n3. Sauvegarde des biomes...");
        String dossierResultats = "./resultats/" + nomPlanete;
        visuBiomes.sauvegarderTousBiomes(image, biomes, dossierResultats, nomPlanete);

        // 5. Détecter les écosystèmes pour chaque biome
        System.out.println("\n4. Détection des écosystèmes...");
        String[] etiquettes = visuBiomes.etiquerBiomes(biomes);
        detecterEcosystemes(image, biomes, etiquettes, dossierResultats + "/ecosystemes");

        // 6. Validation des résultats
        System.out.println("\n5. Validation des résultats...");
        validerResultats(image, biomes, dossierResultats + "/validation");

        System.out.println("\n=== ANALYSE TERMINÉE ===");
        System.out.println("Résultats sauvegardés dans: " + dossierResultats);
    }

    /**
     * Applique le filtre flou gaussien à l'image.
     */
    private String appliquerFiltre(String cheminImage, String nomPlanete) throws IOException {
        // Paramètres du filtre (à ajuster selon les besoins)
        int tailleFiltre = 5; // 5x5
        double sigma = 1.5;

        FiltreFlouGaussien filtre = new FiltreFlouGaussien(tailleFiltre, sigma);
        String cheminSortie = "./temp/" + nomPlanete + "_filtree.jpg";

        // Créer le dossier temp s'il n'existe pas
        Files.createDirectories(Paths.get("./temp"));

        filtre.appliquerFiltre(cheminImage, cheminSortie);

        System.out.println("  Filtre appliqué: " + filtre.getNomFiltre() +
                " (taille=" + tailleFiltre + ", sigma=" + sigma + ")");

        return cheminSortie;
    }

    /**
     * Détecte les biomes dans l'image.
     */
    private ResultatClustering detecterBiomes(BufferedImage image, int nombreBiomes) {
        // Utiliser K-Means avec CIELAB pour une meilleure détection des couleurs
        ResultatClustering biomes = manager.clusteriserImage(
                image,
                Algorithmes.kmeans(nombreBiomes),
                TypeClustering.BIOMES_EUCLIDIENNE
        );

        System.out.println("  Biomes détectés: " + biomes.nombreClusters);
        System.out.println("  Temps d'exécution: " + biomes.dureeMs + " ms");

        // Calculer et afficher l'indice Davies-Bouldin
        MetriqueCouleur metrique = new MetriqueCouleur(new NormeCielab());
        double dbIndex = daviesBouldin.calculer(biomes, metrique);
        System.out.println("  Indice Davies-Bouldin: " + String.format("%.3f", dbIndex));

        return biomes;
    }

    /**
     * Détecte les écosystèmes dans chaque biome.
     */
    private void detecterEcosystemes(BufferedImage image,
                                     ResultatClustering biomes,
                                     String[] etiquettes,
                                     String dossierSortie) throws IOException {
        // Créer le dossier de sortie
        Files.createDirectories(Paths.get(dossierSortie));

        for (int i = 0; i < biomes.nombreClusters; i++) {
            PixelData[] pixelsBiome = biomes.getPixelsCluster(i);

            // Ignorer les biomes trop petits
            if (pixelsBiome.length < 100) {
                System.out.println("  Biome " + i + " (" + etiquettes[i] + ") trop petit, ignoré");
                continue;
            }

            System.out.println("\n  Biome " + i + ": " + etiquettes[i] +
                    " (" + pixelsBiome.length + " pixels)");

            // Utiliser DBSCAN pour détecter les écosystèmes
            // Adapter eps selon la taille du biome
            double eps = Math.sqrt(pixelsBiome.length) * 2; // Heuristique
            int minPts = 20;

            ResultatClustering ecosystemes = manager.clusteriserSousEnsemble(
                    pixelsBiome,
                    Algorithmes.dbscanOpti(eps, minPts),
                    TypeClustering.ECOSYSTEMES_POSITION
            );

            System.out.println("    - Écosystèmes détectés: " + ecosystemes.nombreClusters);
            System.out.println("    - Paramètres DBSCAN: eps=" + eps + ", minPts=" + minPts);

            // Calculer le score de silhouette
            MetriquePositionEuclidienne metriquePos = new MetriquePositionEuclidienne();
            double silhouette = silhouetteScore.calculer(ecosystemes, metriquePos);
            System.out.println("    - Score de silhouette: " + String.format("%.3f", silhouette));

            // Sauvegarder les écosystèmes
            visuEcosystemes.sauvegarderEcosystemesBiome(
                    image, pixelsBiome, ecosystemes,
                    dossierSortie, etiquettes[i], i
            );
        }
    }

    /**
     * Effectue la validation des résultats avec différents paramètres.
     */
    private void validerResultats(BufferedImage image,
                                  ResultatClustering biomes,
                                  String dossierSortie) throws IOException {
        // Créer le dossier de validation
        Files.createDirectories(Paths.get(dossierSortie));

        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DE VALIDATION ===\n\n");

        // 1. Tester différentes valeurs de K pour K-Means
        rapport.append("1. Test de différentes valeurs de K (K-Means avec CIELAB):\n");
        rapport.append("K\tDavies-Bouldin\n");

        MetriqueCouleur metrique = new MetriqueCouleur(new NormeCielab());

        for (int k = 3; k <= 10; k++) {
            ResultatClustering resultat = manager.clusteriserImage(
                    image,
                    Algorithmes.kmeans(k),
                    TypeClustering.BIOMES_CIELAB
            );

            double db = daviesBouldin.calculer(resultat, metrique);
            rapport.append(k).append("\t").append(String.format("%.3f", db)).append("\n");
        }

        // 2. Tester différents paramètres pour DBSCAN
        rapport.append("\n2. Test de paramètres DBSCAN (avec distance euclidienne RGB):\n");
        rapport.append("eps\tminPts\tClusters\tSilhouette\n");

        double[] epsValues = {15.0, 20.0, 25.0, 30.0};
        int[] minPtsValues = {30, 50, 70};

        for (double eps : epsValues) {
            for (int minPts : minPtsValues) {
                ResultatClustering resultat = manager.clusteriserImage(
                        image,
                        Algorithmes.dbscan(eps, minPts),
                        TypeClustering.BIOMES_EUCLIDIENNE
                );

                if (resultat.nombreClusters > 0) {
                    double silhouette = silhouetteScore.calculer(resultat, metrique);
                    rapport.append(String.format("%.1f\t%d\t%d\t%.3f\n",
                            eps, minPts, resultat.nombreClusters, silhouette));
                }
            }
        }

        // Sauvegarder le rapport
        Files.write(Paths.get(dossierSortie + "/rapport_validation.txt"),
                rapport.toString().getBytes());

        System.out.println("  Rapport de validation sauvegardé");
    }

    /**
     * Méthode principale pour tester l'analyse.
     */
    public static void main(String[] args) {
        try {
            Analyseur analyse = new Analyseur();

            // Analyser la planète 1
            analyse.analyserExoplanete("./exoplanètes/Planete 1.jpg", "Planete_1", 7);

            // Vous pouvez ajouter d'autres planètes ici
            // analyse.analyserExoplanete("./exoplanètes/Planete 2.jpg", "Planete_2", 5);

        } catch (IOException e) {
            System.err.println("Erreur lors de l'analyse: " + e.getMessage());
            e.printStackTrace();
        }
    }
}