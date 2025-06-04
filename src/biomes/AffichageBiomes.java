package biomes;

import outils.OutilsImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Classe pour visualiser les biomes détecté.
 */
public class AffichageBiomes {

    /**
     * Crée un fond clair de l'image en augmentant les canaux RGB d'un pourcentage donné.
     *
     * @param imageOriginale Image source
     * @param pourcentageAugmentation Pourcentage d'augmentation (ex: 75 pour 75%)
     * @return Image avec fond éclairci
     */
    public static BufferedImage creerFondClair(BufferedImage imageOriginale, int pourcentageAugmentation) {
        int largeur = imageOriginale.getWidth();
        int hauteur = imageOriginale.getHeight();
        BufferedImage fondClair = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                Color couleurOriginale = new Color(imageOriginale.getRGB(x, y));

                // Calcul des nouvelles valeurs selon la formule du sujet
                int nouveauR = Math.round(couleurOriginale.getRed() + (pourcentageAugmentation / 100.0f) * (255 - couleurOriginale.getRed()));
                int nouveauG = Math.round(couleurOriginale.getGreen() + (pourcentageAugmentation / 100.0f) * (255 - couleurOriginale.getGreen()));
                int nouveauB = Math.round(couleurOriginale.getBlue() + (pourcentageAugmentation / 100.0f) * (255 - couleurOriginale.getBlue()));

                // Assurer que les valeurs restent dans [0, 255]
                nouveauR = Math.max(0, Math.min(255, nouveauR));
                nouveauG = Math.max(0, Math.min(255, nouveauG));
                nouveauB = Math.max(0, Math.min(255, nouveauB));

                Color nouvelleCouleur = new Color(nouveauR, nouveauG, nouveauB);
                fondClair.setRGB(x, y, nouvelleCouleur.getRGB());
            }
        }

        return fondClair;
    }

    /**
     * Affiche un biome spécifique en remplaçant ses pixels sur le fond clair.
     *
     * @param imageOriginale Image source
     * @param affectations Tableau des affectations de clusters pour chaque pixel
     * @param numeroCluster Numéro du cluster/biome à afficher
     * @param nomBiome Nom du biome pour le titre
     * @param pourcentageEclaircissement Pourcentage d'éclaircissement du fond (ex: 75)
     * @return Image du biome isolé
     */
    public static BufferedImage afficherBiomeIsole(BufferedImage imageOriginale, int[] affectations, int numeroCluster, String nomBiome,int pourcentageEclaircissement) {

        // Créer le fond clair
        BufferedImage imageBiome = creerFondClair(imageOriginale, pourcentageEclaircissement);

        int largeur = imageOriginale.getWidth();
        int hauteur = imageOriginale.getHeight();

        // Remplacer les pixels du biome par leurs vraies couleurs
        int index = 0;
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                if (affectations[index] == numeroCluster) {
                    // Ce pixel appartient au biome recherché, garder sa couleur originale
                    imageBiome.setRGB(x, y, imageOriginale.getRGB(x, y));
                }
                index++;
            }
        }

        return imageBiome;
    }

    /**
     * Génère et sauvegarde toutes les images de biomes individuels.
     *
     * @param imageOriginale Image source
     * @param affectations Tableau des affectations de clusters
     * @param etiquettes Noms des biomes correspondant à chaque cluster
     * @param dossierDestination Dossier où sauvegarder les images
     * @param pourcentageEclaircissement Pourcentage d'éclaircissement du fond
     * @throws IOException En cas d'erreur de sauvegarde
     */
    public static void genererTousLesBiomes(BufferedImage imageOriginale, int[] affectations, String[] etiquettes, String dossierDestination, int pourcentageEclaircissement) throws IOException {

        for (int i = 0; i < etiquettes.length; i++) {
            BufferedImage imageBiome = afficherBiomeIsole(imageOriginale, affectations, i, etiquettes[i], pourcentageEclaircissement
            );

            // Nettoyer le nom du biome pour le nom de fichier
            String nomFichier = etiquettes[i].replaceAll("[^a-zA-Z0-9]", "_");
            String cheminComplet = dossierDestination + "/biome_" + i + "_" + nomFichier + ".jpg";

            OutilsImage.sauverImage(imageBiome, cheminComplet);
            System.out.println("Biome sauvegardé : " + cheminComplet + " (" + etiquettes[i] + ")");
        }
    }

    /**
     * Ajoute un titre texte sur l'image (optionnel, pour améliorer la visualisation).
     *
     * @param image Image à modifier
     * @param titre Titre à ajouter
     * @return Image avec titre
     */
    public static BufferedImage ajouterTitre(BufferedImage image, String titre) {
        BufferedImage imageAvecTitre = new BufferedImage(
                image.getWidth(),
                image.getHeight() + 30,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = imageAvecTitre.createGraphics();

        // Fond blanc pour le titre
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), 30);

        // Texte du titre
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (image.getWidth() - fm.stringWidth(titre)) / 2;
        g2d.drawString(titre, x, 20);

        // Image principale
        g2d.drawImage(image, 0, 30, null);
        g2d.dispose();

        return imageAvecTitre;
    }
}