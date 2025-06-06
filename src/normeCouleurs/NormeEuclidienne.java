package normeCouleurs;

import java.awt.*;

/**
 * Implémentation de la norme euclidienne pour comparer les couleurs dans l'espace RGB.
 * Calcule la distance géométrique directe entre les composantes RGB des couleurs.
 */
public class NormeEuclidienne implements NormeCouleurs {

    /**
     * Calcule la distance euclidienne entre deux couleurs.
     * Formule: d = (R1-R2)² + (G1-G2)² + (B1-B2)²
     * Note: La racine carrée n'est pas calculée car seule la comparaison relative importe.
     *
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @return Distance euclidienne au carré entre les deux couleurs
     */
    @Override
    public double distanceCouleur(Color c1, Color c2) {
        int dR = c1.getRed() - c2.getRed();
        int dG = c1.getGreen() - c2.getGreen();
        int dB = c1.getBlue() - c2.getBlue();
        return dR * dR + dG * dG + dB * dB;
    }

    @Override
    public String getNom() {
        return "Euclidienne";
    }
}