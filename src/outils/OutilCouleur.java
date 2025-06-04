package outils;

import java.awt.*;

/**
 * Classe utilitaire pour manipuler les couleurs.
 * Fournit des méthodes pour extraire les composantes RGB d'un entier représentant une couleur.
 */
public class OutilCouleur {


    /**
     * Convertit une couleur RGB vers l'espace colorimétrique CIELAB.
     * License from project: Apache License
     * @param R Composante rouge (0-255)
     * @param G Composante verte (0-255)
     * @param B Composante bleue (0-255)
     * @return Tableau contenant les valeurs L*, a*, b*
     */
    public static int[] rgb2lab(int R, int G, int B) {
        // http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f; // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; // R 0..1
        g = G / 255.f; // G 0..1
        b = B / 255.f; // B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;/*from  www. jav a2 s.com*/
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);

        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        int[] lab = new int[3];
        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
        return lab;
    }


    /**
     * Extrait les composantes RGB d'un entier représentant une couleur.
     * L'entier est au format ARGB où les bits sont répartis comme suit :
     * - Bits 16-23 : composante rouge
     * - Bits 8-15  : composante verte
     * - Bits 0-7   : composante bleue
     *
     * @param c Entier représentant la couleur au format ARGB
     * @return Tableau de 3 entiers [rouge, vert, bleu] avec valeurs entre 0 et 255
     */
    public static int[] getTabColor(int c) {
        int blue = c & 0xff;                // Extraction des 8 bits de poids faible (bleu)
        int green = (c & 0xff00) >> 8;      // Extraction des bits 8-15 puis décalage (vert)
        int red = (c & 0xff0000) >> 16;     // Extraction des bits 16-23 puis décalage (rouge)

        return new int[] {red, green, blue};
    }

    public static Color getColor(int[] c){
        return new Color(c[0],c[1],c[0]);
    }
}