package metriques.couleur;

import metriques.MetriqueDistance;
import outils.PixelData;
import normeCouleurs.NormeCouleurs;

/**
 * Adaptateur qui permet d'utiliser les normes de couleurs existantes
 * comme métriques génériques sur des PixelData.
 */
public class MetriqueCouleur implements MetriqueDistance<PixelData> {
    private final NormeCouleurs normeCouleur;

    public MetriqueCouleur(NormeCouleurs normeCouleur) {
        this.normeCouleur = normeCouleur;
    }

    @Override
    public double calculerDistance(PixelData pixel1, PixelData pixel2) {
        return normeCouleur.distanceCouleur(pixel1.getCouleur(), pixel2.getCouleur());
    }

    @Override
    public String getNom() {
        return normeCouleur.getNom() + " - RGB";
    }
}