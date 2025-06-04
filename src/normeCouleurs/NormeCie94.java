package normeCouleurs;

import outils.OutilCouleur;

import java.awt.*;

/**
 * Implémentation de la norme CIE94 pour comparer les couleurs.
 * Amélioration du ΔE*ab qui prend mieux en compte la perception humaine.
 */
public class NormeCie94 implements NormeCouleurs {

    /**
     * Calcule la distance entre deux couleurs selon la norme CIE94.
     * Formule: ΔE*94 = √((ΔL/1)² + (ΔC/SC)² + (ΔH/SH)²)
     *
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @return Distance CIE94 entre les deux couleurs
     */
    @Override
    public double distanceCouleur(Color c1, Color c2) {
        // Conversion RGB vers LAB
        int[] lab1 = OutilCouleur.rgb2lab(c1.getRed(), c1.getGreen(), c1.getBlue());
        int[] lab2 = OutilCouleur.rgb2lab(c2.getRed(), c2.getGreen(), c2.getBlue());

        // Extraction des composantes L*a*b*
        double L1 = lab1[0];
        double a1 = lab1[1];
        double b1 = lab1[2];

        double L2 = lab2[0];
        double a2 = lab2[1];
        double b2 = lab2[2];

        // Calcul des différences
        double deltaL = L1 - L2;

        // Calcul des chromas
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double deltaC = C1 - C2;

        // Calcul de deltaH
        double deltaA = a1 - a2;
        double deltaB = b1 - b2;
        double deltaH_carre = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;

        // Protection contre les valeurs négatives dues aux erreurs d'arrondi
        double deltaH = deltaH_carre > 0 ? Math.sqrt(deltaH_carre) : 0;

        // Calcul des facteurs de pondération
        double SC = 1 + 0.045 * C1;
        double SH = 1 + 0.015 * C1;

        // Calcul final de ΔE*94
        double terme1 = deltaL / 1.0;  // KL = 1 pour les conditions d'observation standards
        double terme2 = deltaC / SC;
        double terme3 = deltaH / SH;

        return Math.sqrt(terme1 * terme1 + terme2 * terme2 + terme3 * terme3);
    }
}