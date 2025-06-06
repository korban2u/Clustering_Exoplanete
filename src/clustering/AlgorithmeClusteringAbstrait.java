package clustering;

import metriques.MetriqueDistance;

/**
 * Classe abstraite qui fournit les fonctionnalités communes à tous les algorithmes.
 *
 * @param <T> Le type de données à clustériser
 */
public abstract class AlgorithmeClusteringAbstrait<T> implements AlgorithmeClustering<T> {

    protected int nombreClusters;
    protected boolean multithreadingActif;
    protected final String nom;

    public AlgorithmeClusteringAbstrait(String nom) {
        this.nom = nom;
        this.multithreadingActif = false; // Désactivé par défaut
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

    @Override
    public void setMultithreading(boolean actif) {
        this.multithreadingActif = actif;
    }

    @Override
    public boolean isMultithreadingActif() {
        return multithreadingActif;
    }

    /**
     * Méthode utilitaire pour diviser un travail en plusieurs threads.
     *
     * @param nbElements Nombre total d'éléments à traiter
     * @param tache La tâche à exécuter pour chaque plage d'indices
     */
    protected void executerEnParallele(int nbElements, TacheParallele tache) {
        if (!multithreadingActif || nbElements < 100) {
            // Exécution séquentielle si pas de multithreading ou peu de données
            tache.executer(0, nbElements);
            return;
        }

        // Nombre de threads basé sur les processeurs disponibles
        int nbThreads = Math.min(Runtime.getRuntime().availableProcessors(), 8);
        int elementsParThread = nbElements / nbThreads;
        Thread[] threads = new Thread[nbThreads];

        // Créer et démarrer les threads
        for (int i = 0; i < nbThreads; i++) {
            final int debut = i * elementsParThread;
            final int fin = (i == nbThreads - 1) ? nbElements : (i + 1) * elementsParThread;

            threads[i] = new Thread(() -> tache.executer(debut, fin));
            threads[i].start();
        }

        // Attendre que tous les threads se terminent
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interruption lors de l'attente des threads", e);
            }
        }
    }

    /**
     * Interface fonctionnelle pour définir une tâche parallèle.
     */
    @FunctionalInterface
    protected interface TacheParallele {
        void executer(int debut, int fin);
    }
}