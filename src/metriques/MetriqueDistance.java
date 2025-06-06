package metriques;

/**
 * Interface générique pour calculer la distance entre deux objets de type T.
 * Peut être utilisée pour n'importe quel type de données (couleurs, positions, etc.)
 *
 * @param <T> Le type de données à comparer
 */
public interface MetriqueDistance<T> {
    /**
     * Calcule la distance entre deux objets.
     *
     * @param obj1 Premier objet
     * @param obj2 Deuxième objet
     * @return La distance entre les deux objets (toujours positive)
     */
    double calculerDistance(T obj1, T obj2);

    /**
     * Retourne le nom de la métrique pour l'affichage.
     *
     * @return Le nom de la métrique
     */
    String getNom();
}