package donnees;

import java.awt.Color;

/**
 * Représente un pixel avec toutes ses informations.
 * Cette classe unifie les données de couleur et de position.
 */
public class PixelData {
    private final int x;
    private final int y;
    private final Color couleur;
    private final int index; // Index dans l'image originale

    public PixelData(int x, int y, Color couleur, int index) {
        this.x = x;
        this.y = y;
        this.couleur = couleur;
        this.index = index;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getCouleur() { return couleur; }
    public int getIndex() { return index; }

    /**
     * Retourne la position sous forme de tableau [x, y]
     */
    public int[] getPosition() {
        return new int[]{x, y};
    }

    /**
     * Retourne la couleur sous forme de tableau [r, g, b]
     */
    public int[] getCouleurRGB() {
        return new int[]{
                couleur.getRed(),
                couleur.getGreen(),
                couleur.getBlue()
        };
    }
}