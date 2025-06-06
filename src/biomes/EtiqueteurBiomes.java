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


    private static final Map<Color, String> COULEURS_BIOMES = new HashMap<>();

    static {
        COULEURS_BIOMES.put(new Color(220, 220, 200), "Tundra");           // #DCDCC8
        COULEURS_BIOMES.put(new Color(45, 70, 45), "Taiga");               // #2D462D
        COULEURS_BIOMES.put(new Color(80, 120, 50), "Foret temperee");     // #507832
        COULEURS_BIOMES.put(new Color(30, 80, 30), "Foret tropicale");     // #1E501E
        COULEURS_BIOMES.put(new Color(180, 160, 80), "Savane");            // #B4A050
        COULEURS_BIOMES.put(new Color(120, 160, 80), "Prairie");           // #78A050
        COULEURS_BIOMES.put(new Color(210, 180, 140), "Desert");           // #D2B48C
        COULEURS_BIOMES.put(new Color(240, 245, 250), "Glacier");          // #F0F5FA
        COULEURS_BIOMES.put(new Color(100, 180, 200), "Eau peu profonde"); // #64B4C8
        COULEURS_BIOMES.put(new Color(20, 50, 80), "Eau profonde");        // #143250
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