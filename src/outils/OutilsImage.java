package outils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Classe utilitaire pour la gestion des images.
 */
public class OutilsImage {

    /**
     * Sauvegarde une image dans un emplacement.
     * @param image              Image à enregistrer.
     * @param cheminDestination Chemin du fichier de destination (format JPG ici).
     * @throws IOException En cas d'échec d'écriture.
     */
    public static void sauverImage(BufferedImage image, String cheminDestination) throws IOException {

        File fichierDestination = new File(cheminDestination);
        File dossierParent = fichierDestination.getParentFile(); // on prend le nom du dossier ou on veut sauvegarder l'image

        // création du dossier de destination si pas déà fait
        if (dossierParent != null && !dossierParent.exists()) {
            dossierParent.mkdirs();
        }

        boolean succes = ImageIO.write(image, "JPG", fichierDestination);
        if (!succes) {
            throw new IOException("Échec de l'écriture de l'image : " + cheminDestination);
        }
    }

    /**
     * Convertit un chemin de fichier en BufferedImage.
     * @param cheminSource Chemin du fichier image source.
     * @return BufferedImage correspondante.
     * @throws IOException En cas de lecture échouée.
     */
    public static BufferedImage convertionCheminEnBufferedImage(String cheminSource) throws IOException {
        BufferedImage source = ImageIO.read(new File(cheminSource));
        if (source == null) {
            throw new IOException("Impossible de lire l'image : " + cheminSource);
        }
        return source;
    }

    // Pour chaque pixel de l'image
    public static int[][] extraireDonneesPixels(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Tableau [nombrePixels][3] pour RGB
        int[][] donnees = new int[width * height][3];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(image.getRGB(x, y));
                donnees[index][0] = pixel.getRed();    // R
                donnees[index][1] = pixel.getGreen();  // G
                donnees[index][2] = pixel.getBlue();   // B
                index++;
            }
        }
        return donnees;
    }

}
