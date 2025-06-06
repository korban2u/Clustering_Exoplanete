package visualisation;

import clustering.ClusteringManager.ResultatClustering;
import outils.PixelData;
import outils.OutilsImage;
import visualisation.BiomeEtiquetage;
import validation.DaviesBouldinIndex;
import validation.SilhouetteScore;
import metriques.couleur.MetriqueCouleur;
import normeCouleurs.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Classe pour visualiser et sauvegarder les biomes détectés.
 */
public class VisualisationBiomes {

    private final BiomeEtiquetage etiquetage;
    private final DaviesBouldinIndex daviesBouldin;
    private final SilhouetteScore silhouetteScore;

    public VisualisationBiomes() {
        this.etiquetage = new BiomeEtiquetage();
        this.daviesBouldin = new DaviesBouldinIndex();
        this.silhouetteScore = new SilhouetteScore();
    }

    /**
     * Crée une image où chaque pixel est remplacé par la couleur moyenne de son cluster.
     *
     * @param imageOriginale L'image d'origine
     * @param resultat Le résultat du clustering
     * @return L'image avec les couleurs moyennes des clusters
     */
    public BufferedImage creerImageBiomes(BufferedImage imageOriginale, ResultatClustering resultat) {
        int largeur = imageOriginale.getWidth();
        int hauteur = imageOriginale.getHeight();
        BufferedImage imageBiomes = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        // Calculer les couleurs moyennes pour chaque cluster
        Color[] couleursMoyennes = new Color[resultat.nombreClusters];
        for (int i = 0; i < resultat.nombreClusters; i++) {
            couleursMoyennes[i] = resultat.getCouleurMoyenneCluster(i);
        }

        // Remplacer chaque pixel par la couleur moyenne de son cluster
        for (PixelData pixel : resultat.pixels) {
            int cluster = resultat.affectations[pixel.getIndex()];
            if (cluster >= 0) { // Ignorer les points de bruit (-1)
                imageBiomes.setRGB(pixel.getX(), pixel.getY(), couleursMoyennes[cluster].getRGB());
            }
        }

        return imageBiomes;
    }

    /**
     * Crée un fond clair en augmentant les canaux RGB de 75%.
     *
     * @param imageOriginale L'image d'origine
     * @return L'image avec fond clair
     */
    public BufferedImage creerFondClair(BufferedImage imageOriginale) {
        int largeur = imageOriginale.getWidth();
        int hauteur = imageOriginale.getHeight();
        BufferedImage fondClair = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                Color couleur = new Color(imageOriginale.getRGB(x, y));

                // Augmenter chaque canal de 75% de la différence avec 255
                int r = augmenterCanal(couleur.getRed());
                int g = augmenterCanal(couleur.getGreen());
                int b = augmenterCanal(couleur.getBlue());

                fondClair.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return fondClair;
    }

    /**
     * Augmente un canal de couleur de 75% vers 255.
     */
    private int augmenterCanal(int valeur) {
        return (int) Math.round(valeur + 0.75 * (255 - valeur));
    }

    /**
     * Crée une image montrant uniquement un biome spécifique sur fond clair.
     *
     * @param imageOriginale L'image d'origine
     * @param resultat Le résultat du clustering
     * @param clusterBiome Le numéro du cluster du biome à afficher
     * @return L'image du biome isolé
     */
    public BufferedImage creerImageBiomeIsole(BufferedImage imageOriginale,
                                              ResultatClustering resultat,
                                              int clusterBiome) {
        // Créer le fond clair
        BufferedImage imageBiome = creerFondClair(imageOriginale);

        // Remplacer les pixels du biome par les vraies couleurs
        for (PixelData pixel : resultat.pixels) {
            if (resultat.affectations[pixel.getIndex()] == clusterBiome) {
                imageBiome.setRGB(pixel.getX(), pixel.getY(),
                        imageOriginale.getRGB(pixel.getX(), pixel.getY()));
            }
        }

        return imageBiome;
    }

    /**
     * Étiquette chaque cluster selon la table de correspondance couleur-biome.
     *
     * @param resultat Le résultat du clustering
     * @return Un tableau avec le nom du biome pour chaque cluster
     */
    public String[] etiquerBiomes(ResultatClustering resultat) {
        String[] etiquettes = new String[resultat.nombreClusters];

        for (int i = 0; i < resultat.nombreClusters; i++) {
            Color couleurMoyenne = resultat.getCouleurMoyenneCluster(i);
            etiquettes[i] = etiquetage.trouverBiome(couleurMoyenne);
        }

        return etiquettes;
    }

    /**
     * Sauvegarde toutes les images de biomes dans un dossier structuré.
     *
     * @param imageOriginale L'image d'origine
     * @param resultat Le résultat du clustering
     * @param dossierSortie Le dossier de base pour la sauvegarde
     * @param nomImage Le nom de base de l'image
     */
    public void sauvegarderTousBiomes(BufferedImage imageOriginale,
                                      ResultatClustering resultat,
                                      String dossierSortie,
                                      String nomImage) throws IOException {
        // Créer la structure de dossiers
        String dossierBiomes = dossierSortie + "/biomes/" + nomImage;

        // 1. Sauvegarder l'image globale des biomes
        BufferedImage imageBiomes = creerImageBiomes(imageOriginale, resultat);
        OutilsImage.sauverImage(imageBiomes, dossierBiomes + "/biomes_detectes.jpg");

        // 2. Étiqueter les biomes
        String[] etiquettes = etiquerBiomes(resultat);

        // 3. Sauvegarder chaque biome individuellement
        for (int i = 0; i < resultat.nombreClusters; i++) {
            BufferedImage biomeIsole = creerImageBiomeIsole(imageOriginale, resultat, i);
            String nomFichier = String.format("%s/biome_%02d_%s.jpg",
                    dossierBiomes, i,
                    etiquettes[i].replace(" ", "_"));
            OutilsImage.sauverImage(biomeIsole, nomFichier);
        }

        // 4. Créer un fichier récapitulatif avec indices de validation
        creerFichierRecapitulatif(resultat, etiquettes, dossierBiomes + "/rapport_biomes.txt");
    }

    /**
     * Crée un fichier texte récapitulatif des biomes détectés avec indices de validation.
     */
    private void creerFichierRecapitulatif(ResultatClustering resultat,
                                           String[] etiquettes,
                                           String cheminFichier) throws IOException {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DE DÉTECTION DES BIOMES ===\n\n");
        rapport.append("Algorithme utilisé: ").append(resultat.algorithme).append("\n");
        rapport.append("Métrique de distance: ").append(resultat.metrique).append("\n");
        rapport.append("Temps d'exécution: ").append(resultat.dureeMs).append(" ms\n");
        rapport.append("Nombre de biomes détectés: ").append(resultat.nombreClusters).append("\n\n");

        // AJOUT DES INDICES DE VALIDATION
        rapport.append("=== INDICES DE VALIDATION ===\n");

        // Calculer les indices selon la métrique utilisée
        try {
            NormeCouleurs norme = determinerNormeCouleur(resultat.metrique);
            MetriqueCouleur metrique = new MetriqueCouleur(norme);

            // Davies-Bouldin (pour K-Means principalement)
            if (resultat.algorithme.contains("K-Means")) {
                double dbIndex = daviesBouldin.calculer(resultat, metrique);
                rapport.append(String.format("Indice de Davies-Bouldin: %.4f\n", dbIndex));
                rapport.append("  → Plus l'indice est faible, meilleur est le clustering\n");
                rapport.append("  → Valeurs typiques: 0.5-2.0 (< 1.0 = bon clustering)\n\n");
            }

            // Silhouette (pour DBSCAN)
            if(resultat.algorithme.contains("DBSCAN")){
                double silhouette = silhouetteScore.calculer(resultat, metrique);
                rapport.append(String.format("Score de Silhouette: %.4f\n", silhouette));
                rapport.append("  → Valeur entre -1 et 1, plus proche de 1 = meilleur\n");
                rapport.append("  → Interprétation: ");
                if (silhouette > 0.7) rapport.append("Structure forte");
                else if (silhouette > 0.5) rapport.append("Structure raisonnable");
                else if (silhouette > 0.25) rapport.append("Structure faible");
                else rapport.append("Pas de structure claire");
                rapport.append("\n\n");
            }


        } catch (Exception e) {
            rapport.append("Erreur lors du calcul des indices: ").append(e.getMessage()).append("\n\n");
        }

        rapport.append("=== DÉTAIL DES BIOMES ===\n");
        for (int i = 0; i < resultat.nombreClusters; i++) {
            Color couleur = resultat.getCouleurMoyenneCluster(i);
            int nbPixels = resultat.getPixelsCluster(i).length;
            double pourcentage = (nbPixels * 100.0) / resultat.pixels.length;

            rapport.append(String.format("\nBiome %d: %s\n", i, etiquettes[i]));
            rapport.append(String.format("  - Couleur moyenne: RGB(%d, %d, %d)\n",
                    couleur.getRed(), couleur.getGreen(), couleur.getBlue()));
            rapport.append(String.format("  - Nombre de pixels: %d (%.2f%%)\n",
                    nbPixels, pourcentage));

            // Calculer la dispersion du biome
            double dispersion = calculerDispersionBiome(resultat, i);
            rapport.append(String.format("  - Dispersion interne: %.2f\n", dispersion));
        }

        // Écrire le fichier
        java.nio.file.Files.write(java.nio.file.Paths.get(cheminFichier),
                rapport.toString().getBytes());
    }

    /**
     * Détermine la norme de couleur à partir du nom de la métrique.
     */
    private NormeCouleurs determinerNormeCouleur(String nomMetrique) {
        if (nomMetrique.contains("CIELAB")) {
            return new NormeCielab();
        } else if (nomMetrique.contains("CIE94")) {
            return new NormeCie94();
        } else if (nomMetrique.contains("Redmean")) {
            return new NormeRedmean();
        } else {
            return new NormeEuclidienne();
        }
    }

    /**
     * Calcule la dispersion d'un biome (écart-type des distances au centroïde).
     */
    private double calculerDispersionBiome(ResultatClustering resultat, int biome) {
        PixelData[] pixels = resultat.getPixelsCluster(biome);
        if (pixels.length == 0) return 0.0;

        Color couleurMoyenne = resultat.getCouleurMoyenneCluster(biome);
        double sommeCarres = 0.0;

        for (PixelData pixel : pixels) {
            Color c = pixel.getCouleur();
            double dr = c.getRed() - couleurMoyenne.getRed();
            double dg = c.getGreen() - couleurMoyenne.getGreen();
            double db = c.getBlue() - couleurMoyenne.getBlue();
            sommeCarres += dr*dr + dg*dg + db*db;
        }

        return Math.sqrt(sommeCarres / pixels.length);
    }
}