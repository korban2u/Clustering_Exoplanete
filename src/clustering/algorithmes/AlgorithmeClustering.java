package clustering.algorithmes;

import metriques.MetriqueDistance;
import outils.PixelData;

/**
 * Interface pour tous les algorithmes de clustering sur des PixelData
 */
public interface AlgorithmeClustering {

    /**
     * Effectue le clustering sur les données.
     *
     * @param donnees Tableau des PixelData à clustériser
     * @param metrique La métrique de distance à utiliser
     * @return Tableau des affectations de clusters pour chaque donnée
     */
    int[] executer(PixelData[] donnees, MetriqueDistance metrique);

    /**
     * Retourne le nombre de clusters trouvés ou créés.
     *
     * @return Nombre de clusters
     */
    int getNombreClusters();

    /**
     * Retourne le nom de l'algorithme.
     *
     * @return Nom de l'algorithme
     */
    String getNom();
}