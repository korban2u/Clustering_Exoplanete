package metriques;

import outils.PixelData;

/**
 * Interface pour calculer la distance entre deux PixelData.
 */
public interface MetriqueDistance {
    /**
     * Calcule la distance entre deux pixels.
     *
     * @param pixel1 Premier pixel
     * @param pixel2 Deuxième pixel
     * @return La distance entre les deux pixels (toujours positive)
     */
    double calculerDistance(PixelData pixel1, PixelData pixel2);

    /**
     * Retourne le nom de la métrique pour l'affichage.
     *
     * @return Le nom de la métrique
     */
    String getNom();
}