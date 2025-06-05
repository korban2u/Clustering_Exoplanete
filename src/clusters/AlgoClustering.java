package clusters;

/**
 * Interface pour les algorithmes de clustering.
 * Permet d'implémenter différents types d'algorithmes avec ou sans nombre de clusters prédéfini.
 */
public interface AlgoClustering {
    /**
     * Effectue le clustering sur les données.
     * @param objets Tableau de données [nbObjets][nbCaracteristiques]
     * @return Tableau des affectations de clusters pour chaque objet
     */
    public int[] classifier(int[][] objets);

    /**
     * Retourne le nombre de clusters trouvés.
     * @return Nombre de clusters
     */
    public int getNombreClusters();

    /**
     * Retourne le nom de l'algorithme.
     * @return Nom de l'algorithme
     */
    public String getNomAlgorithme();
}