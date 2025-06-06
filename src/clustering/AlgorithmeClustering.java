package clustering;

import metriques.MetriqueDistance;

/**
 * Interface générique pour tous les algorithmes de clustering.
 *
 * @param <T> Le type de données à clustériser
 */
public interface AlgorithmeClustering<T> {

    /**
     * Effectue le clustering sur les données.
     *
     * @param donnees Tableau des données à clustériser
     * @param metrique La métrique de distance à utiliser
     * @return Tableau des affectations de clusters pour chaque donnée
     */
    int[] executer(T[] donnees, MetriqueDistance<T> metrique);

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