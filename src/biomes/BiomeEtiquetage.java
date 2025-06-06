package biomes;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour gérer la correspondance entre couleurs et noms de biomes.
 * Basée sur l'exemple donné dans le sujet (Figure 6).
 */
public class BiomeEtiquetage {

    private final Map<String, Color> biomesReference;

    public BiomeEtiquetage() {
        biomesReference = new HashMap<>();
        initialiserBiomesReference();
    }

    /**
     * Initialise les couleurs de référence pour chaque type de biome.
     */
    private void initialiserBiomesReference() {
        // Selon l'exemple de la Figure 6 du sujet
        biomesReference.put("Océan profond", new Color(0, 0, 128));      // Bleu foncé
        biomesReference.put("Eau peu profonde", new Color(0, 191, 255)); // Bleu clair
        biomesReference.put("Plage", new Color(255, 255, 224));          // Beige
        biomesReference.put("Plaines", new Color(0, 255, 0));            // Vert
        biomesReference.put("Forêt", new Color(0, 128, 0));              // Vert foncé
        biomesReference.put("Savane", new Color(255, 215, 0));           // Jaune doré
        biomesReference.put("Désert", new Color(255, 165, 0));           // Orange
        biomesReference.put("Montagne", new Color(128, 128, 128));       // Gris
        biomesReference.put("Neige", new Color(255, 255, 255));          // Blanc
    }

    /**
     * Trouve le nom du biome le plus proche d'une couleur donnée.
     *
     * @param couleur La couleur à identifier
     * @return Le nom du biome correspondant
     */
    public String trouverBiome(Color couleur) {
        String biomeLePlusProche = "Inconnu";
        double distanceMin = Double.MAX_VALUE;

        for (Map.Entry<String, Color> entry : biomesReference.entrySet()) {
            double distance = calculerDistanceRGB(couleur, entry.getValue());
            if (distance < distanceMin) {
                distanceMin = distance;
                biomeLePlusProche = entry.getKey();
            }
        }

        return biomeLePlusProche;
    }

    /**
     * Calcule la distance euclidienne entre deux couleurs dans l'espace RGB.
     */
    private double calculerDistanceRGB(Color c1, Color c2) {
        int dR = c1.getRed() - c2.getRed();
        int dG = c1.getGreen() - c2.getGreen();
        int dB = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dR * dR + dG * dG + dB * dB);
    }

    /**
     * Retourne la couleur de référence d'un biome.
     */
    public Color getCouleurBiome(String nomBiome) {
        return biomesReference.getOrDefault(nomBiome, Color.BLACK);
    }

    /**
     * Retourne tous les noms de biomes disponibles.
     */
    public String[] getNomsBiomes() {
        return biomesReference.keySet().toArray(new String[0]);
    }
}