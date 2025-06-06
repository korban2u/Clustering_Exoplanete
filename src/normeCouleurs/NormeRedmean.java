package normeCouleurs;

import java.awt.*;

/**
 * Implémentation de la norme "redmean" pour comparer les couleurs.
 * Cette méthode pondère les composantes RGB pour mieux correspondre à la perception humaine.
 * Approximation à faible coût qui donne de meilleurs résultats que la distance euclidienne simple.
 */
public class NormeRedmean implements NormeCouleurs {

    /**
     * Calcule la distance entre deux couleurs selon la méthode "redmean".
     * Formule: ΔC = √((2 + r̄/256)ΔR² + 4ΔG² + (2 + (255-r̄)/256)ΔB²)
     * où r̄ = (R1 + R2)/2
     *
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @return Distance redmean entre les deux couleurs
     */
    @Override
    public double distanceCouleur(Color c1, Color c2) {
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();

        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();

        // Calcul de la moyenne des rouges
        double rBar = (r1 + r2) / 2.0;

        // Calcul des différences
        int deltaR = r1 - r2;
        int deltaG = g1 - g2;
        int deltaB = b1 - b2;

        // Calcul des poids
        double weightR = 2 + (rBar / 256.0);
        double weightB = 2 + ((255 - rBar) / 256.0);

        // Distance redmean
        return Math.sqrt(weightR * deltaR * deltaR + 4 * deltaG * deltaG + weightB * deltaB * deltaB);
    }

    @Override
    public String getNom() {
        return "Redmean";
    }
}