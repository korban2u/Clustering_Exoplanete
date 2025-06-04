package biomes;

import clusters.KMeans;
import clusters.VisualisationBiomes;
import normeCouleurs.NormeCie94;
import normeCouleurs.NormeCouleurs;
import outils.OutilsImage;
import outils.Palette;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static outils.OutilsImage.extraireDonneesPixels;

/**
 * Classe principale intégrant la détection, l'étiquetage et l'affichage des biomes.
 * Implémente les sections 2.1, 2.2 et 2.3 de la SAE.
 */
public class MainBiomes {

    public static void main(String[] args) throws IOException {
        // Test avec l'image filtrée
        String cheminImage = "./resultatFiltre/Planet_1_FlouGauss7x7.jpg";
        int nbBiomes = 7;

        // Lancer l'analyse complète
        analyseCompleteDesBiomes(cheminImage, nbBiomes);
    }

    /**
     * Analyse complète des biomes : détection, étiquetage et visualisation.
     *
     * @param cheminImage Chemin vers l'image à analyser
     * @param nbBiomes Nombre de biomes à détecter
     * @throws IOException En cas d'erreur de lecture/écriture
     */
    public static void analyseCompleteDesBiomes(String cheminImage, int nbBiomes) throws IOException {
        System.out.println("=== ANALYSE COMPLÈTE DES BIOMES ===");
        System.out.println("Image source : " + cheminImage);
        System.out.println("Nombre de biomes à détecter : " + nbBiomes);
        System.out.println();

        // ================================
        // ÉTAPE 2.1 : DÉTECTION DES BIOMES
        // ================================
        System.out.println("🔍 Étape 2.1 : Détection des groupes de pixels similaires...");

        // 1. Charger l'image
        BufferedImage image = OutilsImage.convertionCheminEnBufferedImage(cheminImage);
        System.out.println("Image chargée : " + image.getWidth() + "x" + image.getHeight() + " pixels");

        // 2. Extraire les données RGB
        int[][] donneesRGB = extraireDonneesPixels(image);
        System.out.println("Données extraites : " + donneesRGB.length + " pixels");

        // 3. Clustering K-Means avec norme CIE94
        NormeCouleurs normeCie94 = new NormeCie94();
        KMeans kmeans = new KMeans(normeCie94);
        System.out.println("Lancement du clustering K-Means...");
        int[] affectations = kmeans.classifier(donneesRGB, nbBiomes);

        // 4. Créer palette des biomes trouvés
        Palette paletteBiomes = kmeans.creerPaletteBiomes(kmeans.getCentroides());
        System.out.println("✅ Clustering terminé, " + nbBiomes + " clusters détectés");
        System.out.println();

        // ===============================
        // ÉTAPE 2.2 : ÉTIQUETAGE DES BIOMES
        // ===============================
        System.out.println("🏷️  Étape 2.2 : Étiquetage des biomes...");

        // Étiqueter automatiquement les biomes détectés
        String[] etiquettes = EtiqueteurBiomes.etiqueterTousLesBiomes(kmeans.getCentroides());

        System.out.println("Biomes détectés et étiquetés :");
        for (int i = 0; i < etiquettes.length; i++) {
            int[] centroide = kmeans.getCentroides()[i];
            System.out.printf("  Cluster %d : %s (RGB: %d,%d,%d)%n",
                    i, etiquettes[i], centroide[0], centroide[1], centroide[2]);
        }
        System.out.println();

        // ====================================
        // ÉTAPE 2.3 : AFFICHAGE DES BIOMES
        // ====================================
        System.out.println("🎨 Étape 2.3 : Génération des visualisations...");

        // 5. Visualiser le résultat global (tous les biomes sur une image)
        BufferedImage imageResultatGlobal = VisualisationBiomes.visualiserBiomes(
                image, affectations, paletteBiomes);
        OutilsImage.sauverImage(imageResultatGlobal, "./resultats/tous_biomes_detectes.jpg");
        System.out.println("✅ Image globale sauvegardée : ./resultats/tous_biomes_detectes.jpg");

        // 6. Générer les images individuelles de chaque biome
        int pourcentageEclaircissement = 75; // 75% comme dans l'exemple du sujet
        AffichageBiomes.genererTousLesBiomes(
                image,
                affectations,
                etiquettes,
                "./resultats/biomes_individuels",
                pourcentageEclaircissement
        );
        System.out.println("✅ Images individuelles sauvegardées dans : ./resultats/biomes_individuels/");

        // 7. Générer un rapport de synthèse
        genererRapportSynthese(etiquettes, kmeans.getCentroides(), affectations);

        System.out.println();
        System.out.println("🎉 Analyse complète terminée avec succès !");
    }

    /**
     * Génère un rapport de synthèse des biomes détectés.
     *
     * @param etiquettes Noms des biomes
     * @param centroides Centroïdes des clusters
     * @param affectations Affectations des pixels
     */
    private static void genererRapportSynthese(String[] etiquettes, int[][] centroides, int[] affectations) {
        System.out.println("📊 RAPPORT DE SYNTHÈSE :");
        System.out.println("========================");

        // Calculer les statistiques pour chaque biome
        int[] compteurs = new int[etiquettes.length];
        for (int affectation : affectations) {
            compteurs[affectation]++;
        }

        int totalPixels = affectations.length;

        for (int i = 0; i < etiquettes.length; i++) {
            double pourcentage = (compteurs[i] * 100.0) / totalPixels;
            System.out.printf("  %s : %d pixels (%.1f%%) - Couleur moyenne: RGB(%d,%d,%d)%n",
                    etiquettes[i],
                    compteurs[i],
                    pourcentage,
                    centroides[i][0],
                    centroides[i][1],
                    centroides[i][2]);
        }
        System.out.println("========================");
    }

    /**
     * Méthode pour tester avec différents paramètres.
     *
     * @param cheminImage Chemin de l'image
     * @param nbBiomesMin Nombre minimum de biomes à tester
     * @param nbBiomesMax Nombre maximum de biomes à tester
     * @throws IOException En cas d'erreur
     */
    public static void testerDifferentsNombresBiomes(String cheminImage, int nbBiomesMin, int nbBiomesMax) throws IOException {
        System.out.println("=== TEST DE DIFFÉRENTS NOMBRES DE BIOMES ===");

        for (int nbBiomes = nbBiomesMin; nbBiomes <= nbBiomesMax; nbBiomes++) {
            System.out.println("\n--- Test avec " + nbBiomes + " biomes ---");
            try {
                analyseCompleteDesBiomes(cheminImage, nbBiomes);
            } catch (Exception e) {
                System.err.println("Erreur avec " + nbBiomes + " biomes : " + e.getMessage());
            }
        }
    }
}