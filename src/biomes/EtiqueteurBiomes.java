package biomes;

import normeCouleurs.NormeCouleurs;
import normeCouleurs.NormeCie94;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour l'étiquetage automatique des biomes basé sur la correspondance couleur-nom.
 */
public class EtiqueteurBiomes {

    // Correspondance couleur RGB -> nom de biome (basé sur la figure 6 du sujet)
    private static final Map<Color, String> COULEURS_BIOMES = new HashMap<>();

    static {
        // Initialisation des couleurs de référence des biomes selon la palette fournie
        COULEURS_BIOMES.put(new Color(71, 70, 61), "Tundra");
        COULEURS_BIOMES.put(new Color(43, 50, 35), "Taiga");
        COULEURS_BIOMES.put(new Color(59, 66, 43), "Foret temperee");
        COULEURS_BIOMES.put(new Color(46, 64, 34), "Foret tropicale");
        COULEURS_BIOMES.put(new Color(84, 106, 70), "Savane");
        COULEURS_BIOMES.put(new Color(104, 95, 82), "Prairie");
        COULEURS_BIOMES.put(new Color(152, 140, 120), "Desert");
        COULEURS_BIOMES.put(new Color(200, 200, 200), "Glacier");
        COULEURS_BIOMES.put(new Color(49, 83, 100), "Eau peu profonde");
        COULEURS_BIOMES.put(new Color(12, 31, 47), "Eau profonde");
    }

    /**
     * Trouve le nom du biome le plus proche d'une couleur donnée.
     * Utilise la norme CIE94 pour une meilleure perception des couleurs.
     *
     * @param couleurCentroide Couleur du centroïde du cluster
     * @return Nom du biome correspondant
     */
    public static String etiqueterBiome(Color couleurCentroide) {
        return etiqueterBiome(couleurCentroide, new NormeCie94());
    }

    /**
     * Trouve le nom du biome le plus proche d'une couleur donnée avec une norme spécifique.
     *
     * @param couleurCentroide Couleur du centroïde du cluster
     * @param norme            Norme de couleur à utiliser pour la comparaison
     * @return Nom du biome correspondant
     */
    public static String etiqueterBiome(Color couleurCentroide, NormeCouleurs norme) {
        String biomeLePlusProche = "Biome inconnu";
        double distanceMinimale = Double.MAX_VALUE;

        for (Map.Entry<Color, String> entry : COULEURS_BIOMES.entrySet()) {
            Color couleurReference = entry.getKey();
            String nomBiome = entry.getValue();

            // Utilisation de la norme spécifiée pour calculer la distance
            double distance = norme.distanceCouleur(couleurCentroide, couleurReference);

            if (distance < distanceMinimale) {
                distanceMinimale = distance;
                biomeLePlusProche = nomBiome;
            }
        }

        return biomeLePlusProche;
    }


    /**
     * Étiquette tous les biomes trouvés par clustering.
     * Utilise la norme CIE94 par défaut pour la cohérence avec le clustering.
     *
     * @param centroides Tableau des centroïdes des clusters [nbClusters][3]
     * @return Tableau des noms de biomes correspondants
     */
    public static String[] etiqueterTousLesBiomes(int[][] centroides) {
        return etiqueterTousLesBiomes(centroides, new NormeCie94());
    }

    /**
     * Étiquette tous les biomes trouvés par clustering avec une norme spécifique.
     *
     * @param centroides Tableau des centroïdes des clusters [nbClusters][3]
     * @param norme      Norme de couleur à utiliser
     * @return Tableau des noms de biomes correspondants
     */
    public static String[] etiqueterTousLesBiomes(int[][] centroides, NormeCouleurs norme) {
        String[] etiquettes = new String[centroides.length];

        for (int i = 0; i < centroides.length; i++) {
            Color couleurCentroide = new Color(
                    Math.max(0, Math.min(255, centroides[i][0])),  // R
                    Math.max(0, Math.min(255, centroides[i][1])),  // G
                    Math.max(0, Math.min(255, centroides[i][2]))   // B
            );
            etiquettes[i] = etiqueterBiome(couleurCentroide, norme);
        }

        return etiquettes;
    }

    /**
     * Ajoute une nouvelle correspondance couleur-biome.
     *
     * @param couleur  Couleur de référence
     * @param nomBiome Nom du biome
     */
    public static void ajouterBiome(Color couleur, String nomBiome) {
        COULEURS_BIOMES.put(couleur, nomBiome);
    }

    /**
     * Retourne la carte complète des couleurs-biomes.
     *
     * @return Map des correspondances couleur-nom
     */
    public static Map<Color, String> getCouleursBiomes() {
        return new HashMap<>(COULEURS_BIOMES);
    }
}