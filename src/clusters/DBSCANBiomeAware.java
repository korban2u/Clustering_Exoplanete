package clusters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Version encore plus optimisée qui utilise directement les informations des biomes
 */
public class DBSCANBiomeAware extends DBSCANOptimise {

    private int[] affectationsBiomes;
    private int biomeActuel;

    public DBSCANBiomeAware(double eps, int minPts) {
        super(eps, minPts);
    }

    /**
     * Configure les informations de biomes pour l'optimisation
     */
    public void configurerBiomes(int[] affectationsBiomes, int biomeActuel) {
        this.affectationsBiomes = affectationsBiomes;
        this.biomeActuel = biomeActuel;
    }

    /**
     * Classifie uniquement les pixels d'un biome spécifique
     */
    public int[] classifierBiome(int[][] positions, Map<Integer, Integer> indexMapping) {
        // Ici, positions contient uniquement les pixels du biome
        // et indexMapping fait la correspondance avec les indices originaux
        return super.classifier(positions);
    }

    /**
     * Version ultra-rapide qui pré-filtre par biome
     */
    @Override
    protected List<Integer> regionQueryOptimisee(int[][] objets, int pointIndex) {
        List<Integer> voisins = super.regionQueryOptimisee(objets, pointIndex);

        // Si on a les affectations de biomes, on peut filtrer encore plus
        if (affectationsBiomes != null) {
            List<Integer> voisinsDuMemeBiome = new ArrayList<>();
            for (int i : voisins) {
                // Vérifier que le voisin est du même biome
                if (affectationsBiomes[i] == biomeActuel) {
                    voisinsDuMemeBiome.add(i);
                }
            }
            return voisinsDuMemeBiome;
        }

        return voisins;
    }
}
