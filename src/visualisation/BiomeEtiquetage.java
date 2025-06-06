package visualisation;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour gérer la correspondance entre couleurs et noms de biomes.
 * On a utilisé les palettes du sujet
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
        // palette du sujet (bug un peu avec Désert/Glacier et Eau profonde et pas profonde, parfois ça confond)
        biomesReference.put("Tundra", new Color(71, 70, 61));
        biomesReference.put("Taïga", new Color(43, 50, 35));
        biomesReference.put("Forêt tempérée", new Color(59, 66, 43));
        biomesReference.put("Forêt tropicale", new Color(46, 64, 34));
        biomesReference.put("Savane", new Color(84, 106, 70));
        biomesReference.put("Prairie", new Color(104, 95, 82));
        biomesReference.put("Désert", new Color(152, 140, 120));
        biomesReference.put("Glacier", new Color(200, 200, 200));
        biomesReference.put("Eau peu profonde", new Color(49, 83, 100));
        biomesReference.put("Eau profonde", new Color(12, 31, 47));
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