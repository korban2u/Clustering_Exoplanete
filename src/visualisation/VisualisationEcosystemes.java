package visualisation;

import clustering.ClusteringManager.ResultatClustering;
import outils.PixelData;
import outils.OutilsImage;
import validation.SilhouetteScore;
import metriques.position.MetriquePositionEuclidienne;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * Classe pour visualiser et sauvegarder les écosystèmes détectés dans chaque biome.
 */
public class VisualisationEcosystemes {

    // Palette de couleurs prédéfinies pour les écosystèmes
    private final Color[] COULEURS_ECOSYSTEMES = {
            new Color(255, 0, 0),     // Rouge
            new Color(0, 255, 0),     // Vert
            new Color(0, 0, 255),     // Bleu
            new Color(255, 255, 0),   // Jaune
            new Color(255, 0, 255),   // Magenta
            new Color(0, 255, 255),   // Cyan
            new Color(255, 128, 0),   // Orange
            new Color(128, 0, 255),   // Violet
            new Color(0, 255, 128),   // Vert menthe
            new Color(255, 0, 128),   // Rose
            new Color(128, 255, 0),   // Vert lime
            new Color(0, 128, 255),   // Bleu ciel
            new Color(255, 192, 192), // Rose pâle
            new Color(192, 255, 192), // Vert pâle
            new Color(192, 192, 255), // Bleu pâle
    };

    private final Random random = new Random();
    private final SilhouetteScore silhouetteScore = new SilhouetteScore();

    /**
     * Crée une image montrant les écosystèmes d'un biome avec des couleurs distinctes.
     *
     * @param largeur Largeur de l'image
     * @param hauteur Hauteur de l'image
     * @param pixelsBiome Les pixels du biome
     * @param resultatEcosystemes Le résultat du clustering des écosystèmes
     * @return L'image avec les écosystèmes colorés
     */
    public BufferedImage creerImageEcosystemes(int largeur, int hauteur,
                                               PixelData[] pixelsBiome,
                                               ResultatClustering resultatEcosystemes) {
        // Créer une image avec fond blanc
        BufferedImage image = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, largeur, hauteur);
        g2d.dispose();

        // Assigner une couleur à chaque écosystème
        Color[] couleursEcosystemes = assignerCouleursEcosystemes(resultatEcosystemes.nombreClusters);

        // Colorier chaque pixel selon son écosystème
        for (int i = 0; i < pixelsBiome.length; i++) {
            PixelData pixel = pixelsBiome[i];
            int ecosysteme = resultatEcosystemes.affectations[i];

            if (ecosysteme >= 0) { // Ignorer les points de bruit
                Color couleur = couleursEcosystemes[ecosysteme];
                image.setRGB(pixel.getX(), pixel.getY(), couleur.getRGB());
            }
        }

        return image;
    }

    /**
     * Crée une image montrant les écosystèmes sur fond clair.
     */
    public BufferedImage creerImageEcosystemesSurFondClair(BufferedImage fondClair,
                                                           PixelData[] pixelsBiome,
                                                           ResultatClustering resultatEcosystemes) {
        // Copier le fond clair
        BufferedImage image = new BufferedImage(fondClair.getWidth(),
                fondClair.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(fondClair, 0, 0, null);
        g.dispose();

        // Assigner une couleur à chaque écosystème
        Color[] couleursEcosystemes = assignerCouleursEcosystemes(resultatEcosystemes.nombreClusters);

        // Colorier chaque pixel selon son écosystème
        for (int i = 0; i < pixelsBiome.length; i++) {
            PixelData pixel = pixelsBiome[i];
            int ecosysteme = resultatEcosystemes.affectations[i];

            if (ecosysteme >= 0) { // Ignorer les points de bruit
                Color couleur = couleursEcosystemes[ecosysteme];
                image.setRGB(pixel.getX(), pixel.getY(), couleur.getRGB());
            }
        }

        return image;
    }

    /**
     * Assigne des couleurs distinctes aux écosystèmes.
     */
    private Color[] assignerCouleursEcosystemes(int nombreEcosystemes) {
        Color[] couleurs = new Color[nombreEcosystemes];

        if (nombreEcosystemes <= COULEURS_ECOSYSTEMES.length) {
            // Utiliser les couleurs prédéfinies
            System.arraycopy(COULEURS_ECOSYSTEMES, 0, couleurs, 0, nombreEcosystemes);
        } else {
            // Si plus d'écosystèmes que de couleurs prédéfinies, générer des couleurs
            for (int i = 0; i < nombreEcosystemes; i++) {
                if (i < COULEURS_ECOSYSTEMES.length) {
                    couleurs[i] = COULEURS_ECOSYSTEMES[i];
                } else {
                    // Générer une couleur aléatoire mais vive
                    couleurs[i] = genererCouleurVive();
                }
            }
        }

        return couleurs;
    }

    /**
     * Génère une couleur vive aléatoire.
     */
    private Color genererCouleurVive() {
        // Au moins une composante à 255 pour avoir une couleur vive
        int composantePrincipale = random.nextInt(3);
        int r = composantePrincipale == 0 ? 255 : random.nextInt(128) + 128;
        int g = composantePrincipale == 1 ? 255 : random.nextInt(128) + 128;
        int b = composantePrincipale == 2 ? 255 : random.nextInt(128) + 128;
        return new Color(r, g, b);
    }

    /**
     * Sauvegarde les écosystèmes d'un biome.
     */
    public void sauvegarderEcosystemesBiome(BufferedImage imageOriginale,
                                            PixelData[] pixelsBiome,
                                            ResultatClustering resultatEcosystemes,
                                            String dossierSortie,
                                            String nomBiome,
                                            int numeroBiome) throws IOException {
        // Créer le dossier pour ce biome
        String dossierBiome = String.format("%s/biome_%02d_%s",
                dossierSortie, numeroBiome,
                nomBiome.replace(" ", "_"));

        // 1. Image des écosystèmes sur fond blanc
        BufferedImage imageEcosystemes = creerImageEcosystemes(
                imageOriginale.getWidth(),
                imageOriginale.getHeight(),
                pixelsBiome,
                resultatEcosystemes
        );
        OutilsImage.sauverImage(imageEcosystemes, dossierBiome + "/ecosystemes.jpg");

        // 2. Image des écosystèmes sur fond clair
        VisualisationBiomes visuBiomes = new VisualisationBiomes();
        BufferedImage fondClair = visuBiomes.creerFondClair(imageOriginale);
        BufferedImage ecosystemesFondClair = creerImageEcosystemesSurFondClair(
                fondClair, pixelsBiome, resultatEcosystemes
        );
        OutilsImage.sauverImage(ecosystemesFondClair, dossierBiome + "/ecosystemes_fond_clair.jpg");

        // 3. Créer un rapport pour ce biome avec indices de validation
        creerRapportEcosystemes(resultatEcosystemes, pixelsBiome, nomBiome,
                dossierBiome + "/rapport_ecosystemes.txt");
    }

    /**
     * Crée un rapport textuel sur les écosystèmes détectés avec indices de validation.
     */
    private void creerRapportEcosystemes(ResultatClustering resultat,
                                         PixelData[] pixelsBiome,
                                         String nomBiome,
                                         String cheminFichier) throws IOException {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT DES ÉCOSYSTÈMES ===\n\n");
        rapport.append("Biome analysé: ").append(nomBiome).append("\n");
        rapport.append("Algorithme utilisé: ").append(resultat.algorithme).append("\n");
        rapport.append("Métrique de distance: ").append(resultat.metrique).append("\n");
        rapport.append("Temps d'exécution: ").append(resultat.dureeMs).append(" ms\n");
        rapport.append("Nombre d'écosystèmes détectés: ").append(resultat.nombreClusters).append("\n\n");

        // AJOUT DES INDICES DE VALIDATION
        rapport.append("=== INDICES DE VALIDATION ===\n");

        try {
            // Silhouette Score pour les écosystèmes
            MetriquePositionEuclidienne metriquePos = new MetriquePositionEuclidienne();
            double silhouette = silhouetteScore.calculer(resultat, metriquePos);

            rapport.append(String.format("Score de Silhouette: %.4f\n", silhouette));
            rapport.append("  → Valeur entre -1 et 1, plus proche de 1 = meilleur\n");
            rapport.append("  → Interprétation: ");
            if (silhouette > 0.7) rapport.append("Écosystèmes très bien séparés");
            else if (silhouette > 0.5) rapport.append("Bonne séparation des écosystèmes");
            else if (silhouette > 0.25) rapport.append("Séparation faible");
            else rapport.append("Écosystèmes mal définis ou chevauchants");
            rapport.append("\n\n");

            // Statistiques supplémentaires pour DBSCAN
            if (resultat.algorithme.contains("DBSCAN")) {
                int pointsBruit = 0;
                for (int aff : resultat.affectations) {
                    if (aff == -1) pointsBruit++;
                }
                double pourcentageBruit = (pointsBruit * 100.0) / resultat.affectations.length;
                rapport.append(String.format("Points de bruit: %d (%.2f%%)\n",
                        pointsBruit, pourcentageBruit));
                rapport.append("  → Un faible pourcentage de bruit (<5%) indique de bons paramètres\n\n");
            }

        } catch (Exception e) {
            rapport.append("Erreur lors du calcul des indices: ").append(e.getMessage()).append("\n\n");
        }

        rapport.append("=== DÉTAIL DES ÉCOSYSTÈMES ===\n");

        // Analyser chaque écosystème
        for (int i = 0; i < resultat.nombreClusters; i++) {
            int nbPixels = 0;
            double sumX = 0, sumY = 0;
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            // Calculer les statistiques de l'écosystème
            for (int j = 0; j < resultat.affectations.length; j++) {
                if (resultat.affectations[j] == i) {
                    nbPixels++;
                    PixelData pixel = pixelsBiome[j];
                    sumX += pixel.getX();
                    sumY += pixel.getY();
                    minX = Math.min(minX, pixel.getX());
                    maxX = Math.max(maxX, pixel.getX());
                    minY = Math.min(minY, pixel.getY());
                    maxY = Math.max(maxY, pixel.getY());
                }
            }

            if (nbPixels > 0) {
                double pourcentage = (nbPixels * 100.0) / pixelsBiome.length;
                double centreX = sumX / nbPixels;
                double centreY = sumY / nbPixels;

                rapport.append(String.format("\nÉcosystème %d:\n", i));
                rapport.append(String.format("  - Nombre de pixels: %d (%.2f%% du biome)\n",
                        nbPixels, pourcentage));
                rapport.append(String.format("  - Position centrale: (%.0f, %.0f)\n",
                        centreX, centreY));
                rapport.append(String.format("  - Étendue spatiale: %.0f x %.0f pixels\n",
                        maxX - minX + 1, maxY - minY + 1));

                // Calculer la compacité (ratio surface/périmètre approximatif)
                double surface = nbPixels;
                double diagonale = Math.sqrt((maxX-minX)*(maxX-minX) + (maxY-minY)*(maxY-minY));
                double compacite = surface / (diagonale * diagonale);
                rapport.append(String.format("  - Compacité: %.3f", compacite));
                if (compacite > 0.7) rapport.append(" (forme compacte)");
                else if (compacite > 0.4) rapport.append(" (forme moyennement compacte)");
                else rapport.append(" (forme étalée ou fragmentée)");
                rapport.append("\n");
            }
        }

        // Ajouter un résumé global
        rapport.append("\n=== RÉSUMÉ ===\n");
        if (resultat.nombreClusters > 5) {
            rapport.append("→ Nombreux écosystèmes détectés, le biome est très fragmenté\n");
        } else if (resultat.nombreClusters > 2) {
            rapport.append("→ Fragmentation modérée du biome\n");
        } else {
            rapport.append("→ Biome peu fragmenté, écosystèmes bien définis\n");
        }

        // Écrire le fichier
        java.nio.file.Files.write(java.nio.file.Paths.get(cheminFichier),
                rapport.toString().getBytes());
    }
}