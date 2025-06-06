package clustering;

import metriques.MetriqueDistance;

/**
 * Classe abstraite qui fournit les fonctionnalités communes à tous les algorithmes.
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