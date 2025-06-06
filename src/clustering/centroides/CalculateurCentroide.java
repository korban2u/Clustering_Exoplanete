package clustering.centroides;

/**
 * Interface pour calculer le centroïde d'un ensemble d'objets.
 * Nécessaire pour K-Means générique car on ne peut pas "moyenner" n'importe quel type T.
 *
 * @param <T> Le type d'objets dont on veut calculer le centroïde
 */
public interface CalculateurCentroide<T> {
    /**
     * Calcule le centroïde (centre) d'un ensemble d'objets.
     *
     * @param objets Les objets dont on veut le centroïde
     * @return Le centroïde calculé
     */
    T calculerCentroide(T[] objets);

    /**
     * Vérifie si le calculateur peut créer de nouveaux objets.
     * Si false, on utilisera un objet existant comme représentant.
     *
     * @return true si on peut créer de nouveaux centroïdes
     */
    boolean peutCreerNouveauxObjets();
}