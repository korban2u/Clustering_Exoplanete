package biomes;

import outils.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;

public class VisualisationBiomes {

    public static BufferedImage visualiserBiomes(BufferedImage imageOriginale, int[] affectations, Palette paletteBiomes) {

        int width = imageOriginale.getWidth();
        int height = imageOriginale.getHeight();
        BufferedImage resultat = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Récupérer le biome du pixel
                int numeroBiome = affectations[index];

                // Utiliser la couleur du biome depuis la palette
                Color couleurBiome = paletteBiomes.getCouleur(numeroBiome);
                resultat.setRGB(x, y, couleurBiome.getRGB());

                index++;
            }
        }
        return resultat;
    }
}