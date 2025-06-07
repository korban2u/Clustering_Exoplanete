package clustering.algorithmes;

/**
 * Classe abstraite qui donne les fonctionnalités communes des algorithmes.
 */
public abstract class AlgorithmeClusteringAbstrait implements AlgorithmeClustering {

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