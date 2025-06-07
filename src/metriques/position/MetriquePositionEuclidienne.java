package metriques.position;

import metriques.MetriqueDistance;
import outils.PixelData;

/**
 * Métrique de distance euclidienne pour les positions de pixels.
 */
public class MetriquePositionEuclidienne implements MetriqueDistance {

    @Override
    public double calculerDistance(PixelData pixel1, PixelData pixel2) {
        double dx = pixel1.getX() - pixel2.getX();
        double dy = pixel1.getY() - pixel2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String getNom() {
        return "Distance Euclidienne - Position";
    }
}