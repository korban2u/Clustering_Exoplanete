package clustering.centroides;

import outils.PixelData;
import java.awt.Color;

/**
 * Calculateur de centroïde pour les PixelData.
 * Peut calculer selon la couleur, la position, ou les deux.
 */
public class CalculateurCentroidePixel implements CalculateurCentroide<PixelData> {

    public enum TypeCentroide {
        COULEUR,     // Moyenne des couleurs seulement
        POSITION,    // Moyenne des positions seulement
        COMPLET      // Moyenne des couleurs ET positions
    }

    private final TypeCentroide type;

    public CalculateurCentroidePixel(TypeCentroide type) {
        this.type = type;
    }

    @Override
    public PixelData calculerCentroide(PixelData[] pixels) {
        if (pixels == null || pixels.length == 0) {
            throw new IllegalArgumentException("Impossible de calculer le centroïde d'un ensemble vide");
        }

        // Calculer les moyennes
        double moyX = 0, moyY = 0;
        double moyR = 0, moyG = 0, moyB = 0;

        for (PixelData pixel : pixels) {
            if (type == TypeCentroide.POSITION || type == TypeCentroide.COMPLET) {
                moyX += pixel.getX();
                moyY += pixel.getY();
            }

            if (type == TypeCentroide.COULEUR || type == TypeCentroide.COMPLET) {
                moyR += pixel.getCouleur().getRed();
                moyG += pixel.getCouleur().getGreen();
                moyB += pixel.getCouleur().getBlue();
            }
        }

        int n = pixels.length;

        // Créer le centroïde
        int x, y;
        Color couleur;

        switch (type) {
            case POSITION:
                // Pour la position, on prend la moyenne
                x = (int) Math.round(moyX / n);
                y = (int) Math.round(moyY / n);
                // On garde la couleur du premier pixel
                couleur = pixels[0].getCouleur();
                break;

            case COULEUR:
                // Pour la couleur, on prend la moyenne
                couleur = new Color(
                        clamp((int) Math.round(moyR / n)),
                        clamp((int) Math.round(moyG / n)),
                        clamp((int) Math.round(moyB / n))
                );
                // On garde la position du premier pixel
                x = pixels[0].getX();
                y = pixels[0].getY();
                break;

            case COMPLET:
                // On prend la moyenne des deux
                x = (int) Math.round(moyX / n);
                y = (int) Math.round(moyY / n);
                couleur = new Color(
                        clamp((int) Math.round(moyR / n)),
                        clamp((int) Math.round(moyG / n)),
                        clamp((int) Math.round(moyB / n))
                );
                break;

            default:
                throw new IllegalStateException("Type de centroïde non géré: " + type);
        }

        // L'index n'a pas vraiment de sens pour un centroïde, on met -1
        return new PixelData(x, y, couleur, -1);
    }


    /**
     * S'assure qu'une valeur est dans l'intervalle [0, 255] pour les couleurs.
     */
    private int clamp(int valeur) {
        return Math.max(0, Math.min(255, valeur));
    }

    /**
     * Méthode alternative : trouve le pixel le plus proche du vrai centroïde.
     * Utile quand on veut un pixel qui existe vraiment dans l'image.
     */
    public PixelData trouverPixelLePlusProcheDuCentroide(PixelData[] pixels) {
        PixelData centroideTheorique = calculerCentroide(pixels);

        PixelData plusProche = pixels[0];
        double distanceMin = distance(centroideTheorique, plusProche);

        for (int i = 1; i < pixels.length; i++) {
            double dist = distance(centroideTheorique, pixels[i]);
            if (dist < distanceMin) {
                distanceMin = dist;
                plusProche = pixels[i];
            }
        }

        return plusProche;
    }

    /**
     * Calcule la distance entre deux pixels selon le type de centroïde.
     */
    private double distance(PixelData p1, PixelData p2) {
        double dist = 0;

        if (type == TypeCentroide.POSITION || type == TypeCentroide.COMPLET) {
            double dx = p1.getX() - p2.getX();
            double dy = p1.getY() - p2.getY();
            dist += dx * dx + dy * dy;
        }

        if (type == TypeCentroide.COULEUR || type == TypeCentroide.COMPLET) {
            Color c1 = p1.getCouleur();
            Color c2 = p2.getCouleur();
            double dr = c1.getRed() - c2.getRed();
            double dg = c1.getGreen() - c2.getGreen();
            double db = c1.getBlue() - c2.getBlue();
            dist += dr * dr + dg * dg + db * db;
        }

        return Math.sqrt(dist);
    }
}