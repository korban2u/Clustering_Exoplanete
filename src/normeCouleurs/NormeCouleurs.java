package normeCouleurs;

import java.awt.*;

public interface NormeCouleurs {
    /**
     * Calcule la distance perceptuelle entre deux couleurs.
     *
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @return une valeur représentant la distance entre les deux couleurs
     */
    double distanceCouleur(Color c1, Color c2);
}
