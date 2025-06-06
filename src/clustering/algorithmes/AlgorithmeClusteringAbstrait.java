package clustering.algorithmes;

/**
 * Classe abstraite qui donne les fonctionnalités en communs des algos.
 *
 * @param <T> Le type de données à clustériser
 */
public abstract class AlgorithmeClusteringAbstrait<T> implements AlgorithmeClustering<T> {

    protected int nombreClusters;
    protected final String nom;

    public AlgorithmeClusteringAbstrait(String nom) {
        this.nom = nom;
        this.nombreClusters = 0;
    }

    @Override
    public int getNombreClusters() {
        return nombreClusters;
    }

    @Override
    public String getNom() {
        return nom;
    }
}