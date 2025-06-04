package filtres;

import outils.OutilCouleur;
import outils.OutilsImage;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Classe abstraite représentant un filtre de flou.
 */
public abstract class FiltreFlou implements Filtre {

    protected int tailleFiltre;

    /**
     * Constructeur pour initialiser la taille du filtre.
     *
     * @param tailleFiltre Taille du filtre (doit être un impair >= 3).
     * @throws IllegalArgumentException si la taille n'est pas valide.
     */
    public FiltreFlou(int tailleFiltre) {
        if (tailleFiltre % 2 == 0 || tailleFiltre < 3) {
            throw new IllegalArgumentException("La taille du filtre doit être un impair >= 3");
        }
        this.tailleFiltre = tailleFiltre;
    }

    /**
     * Méthode à implémenter pour calculer la matrice des coefficients du filtre.
     *
     * @return Matrice des coefficients de convolution.
     */
    protected abstract double[][] calculerCoef();

    @Override
    public void appliquerFiltre(String cheminSource, String cheminDestination) throws IOException {
        BufferedImage image = OutilsImage.convertionCheminEnBufferedImage(cheminSource);
        int largeur = image.getWidth();
        int hauteur = image.getHeight();

        BufferedImage imageRes = new BufferedImage(largeur, hauteur, image.getType());
        double[][] matrice = calculerCoef(); // calcul qui diffère en fonction du type de filtre flou qu'on applique
        int milieu = tailleFiltre / 2; // permet de choper le point au milieu du filtre

        // boucle sur presque tout les pixels,on ne prends pas les coins (car ils ont pas de pixel voisin)
        // la taille des coins depends de la taille du filtre, par ex : pour 3x3 on commence à (1,1)
        for (int y = milieu; y < hauteur - milieu; y++) {
            for (int x = milieu; x < largeur - milieu; x++) {
                double sommeR = 0, sommeG = 0, sommeB = 0;

                // boucle sur les pixels du filtre (ex : si c'est 3x3 la boucle sera de 9)
                for (int dy = -milieu; dy <= milieu; dy++) {
                    for (int dx = -milieu; dx <= milieu; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        int[] tabRGB = OutilCouleur.getTabColor(rgb);
                        double coeff = matrice[dy + milieu][dx + milieu];

                        // on applique les coefs
                        sommeR += tabRGB[0] * coeff;
                        sommeG += tabRGB[1] * coeff;
                        sommeB += tabRGB[2] * coeff;
                    }
                }

                int r = (int) sommeR;
                int g = (int) sommeG;
                int b = (int) sommeB;
                int rgbFinal = (r << 16) | (g << 8) | b;
                imageRes.setRGB(x, y, rgbFinal);
            }
        }

        // sauvegarde l'image
        OutilsImage.sauverImage(imageRes, cheminDestination);
    }
}
