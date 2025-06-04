package normeCouleurs;

import outils.OutilCouleur;

import java.awt.*;

/**
 * Implémentation de la norme CIELAB pour comparer les couleurs.
 * Utilise l'espace colorimétrique L*a*b* qui est perceptuellement uniforme.
 */
public class NormeCielab implements NormeCouleurs {

    /**
     * Calcule la distance entre deux couleurs dans l'espace CIELAB.
     * formule donné : ΔE*ab = √((L*2-L*1)² + (a*2-a*1)² + (b*2-b*1)²)
     *
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @return Distance CIELAB entre les deux couleurs
     */
    @Override
    public double distanceCouleur(Color c1, Color c2) {
        // Conversion RGB vers LAB pour la première couleur
        int[] lab1 = OutilCouleur.rgb2lab(c1.getRed(), c1.getGreen(), c1.getBlue());

        // Conversion RGB vers LAB pour la deuxième couleur
        int[] lab2 = OutilCouleur.rgb2lab(c2.getRed(), c2.getGreen(), c2.getBlue());

        // Calcul des différences
        int deltaL = lab1[0] - lab2[0];
        int deltaA = lab1[1] - lab2[1];
        int deltaB = lab1[2] - lab2[2];

        // Distance euclidienne dans l'espace LAB
        return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    }
}