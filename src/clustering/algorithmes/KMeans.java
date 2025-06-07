package clustering.algorithmes;

import clustering.centroides.CalculateurCentroide;
import clustering.centroides.CalculateurCentroidePixel;
import clustering.centroides.CalculateurCentroidePixel.TypeCentroide;
import outils.PixelData;
import metriques.MetriqueDistance;

import java.util.Random;
import java.util.Arrays;

/**
 * Implémentation de K-Means.
 * Fonctionne avec n'importe quel type de données et métrique.
 *
 * @param <T> Le type de données à clustériser
 */
public class KMeans<T> extends AlgorithmeClusteringAbstrait<T> {

    private final int nbClusters; // Nombre de clusters souhaité
    private final int maxIterations;
    private final Random random;
    private CalculateurCentroide<T> calculateurCentroide;


    public KMeans(int nbClusters, int maxIterations) {
        super("K-Means (K=" + nbClusters + ")");
        this.nbClusters = nbClusters;
        this.maxIterations = maxIterations;
        this.random = new Random();

    }

    /**
     * Définit le calculateur de centroïde à utiliser.
     */
    public void setCalculateurCentroide(CalculateurCentroide<T> calculateur) {
        this.calculateurCentroide = calculateur;
    }

    @Override
    public int[] executer(T[] donnees, MetriqueDistance<T> metrique) {
        int n = donnees.length;
        if (n == 0) return new int[0];

        // Détection automatique du calculateur de centroïde pour PixelData
        if (calculateurCentroide == null && donnees[0] instanceof PixelData) {
            // Déterminer le type selon la métrique
            String nomMetrique = metrique.getNom().toLowerCase();
            if (nomMetrique.contains("position") && !nomMetrique.contains("rgb")) {
                calculateurCentroide = (CalculateurCentroide<T>) new CalculateurCentroidePixel(TypeCentroide.POSITION);
            } else {
                calculateurCentroide = (CalculateurCentroide<T>) new CalculateurCentroidePixel(TypeCentroide.COULEUR);
            }
        }

        // Initialisation
        int[] affectations = new int[n];
        T[] centroides = initialiserCentroides(donnees);

        // Boucle principale
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int[] nouvellesAffectations = new int[n];

            // Étape 1 : Affecter chaque point au centroïde le plus proche

            affecterPointsSequentiel(donnees, centroides, nouvellesAffectations, metrique);


            // Vérifier la convergence
            if (Arrays.equals(affectations, nouvellesAffectations)) {
                break;
            }
            affectations = nouvellesAffectations;

            // Étape 2 : Mettre à jour les centroïdes
            centroides = mettreAJourCentroides(donnees, affectations);
        }

        this.nombreClusters = nbClusters;

        return affectations;
    }

    /**
     * Initialise les centroïdes en choisissant k points aléatoires.
     */
    @SuppressWarnings("unchecked") // on met ça pour eviter avertisements unchecked casts quand on compile (vu que on utilise des types générique)
    private T[] initialiserCentroides(T[] donnees) {
        Class<?> componentType = donnees.getClass().getComponentType();
        T[] centroides = (T[]) java.lang.reflect.Array.newInstance(componentType, nbClusters);
        boolean[] choisis = new boolean[donnees.length];

        for (int i = 0; i < nbClusters; i++) {
            int index;
            do {
                index = random.nextInt(donnees.length);
            } while (choisis[index]);

            choisis[index] = true;
            centroides[i] = donnees[index];
        }

        return centroides;
    }

    /**
     * Affecte chaque point au centroïde le plus proche
     */
    private void affecterPointsSequentiel(T[] donnees, T[] centroides,
                                          int[] affectations, MetriqueDistance<T> metrique) {
        for (int i = 0; i < donnees.length; i++) {
            affectations[i] = trouverCentroideLePlusProche(donnees[i], centroides, metrique);
        }
    }


    /**
     * Trouve l'indice du centroïde le plus proche d'un point donné.
     */
    private int trouverCentroideLePlusProche(T point, T[] centroides, MetriqueDistance<T> metrique) {
        int plusProche = 0;
        double distanceMin = metrique.calculerDistance(point, centroides[0]);

        for (int i = 1; i < centroides.length; i++) {
            double distance = metrique.calculerDistance(point, centroides[i]);
            if (distance < distanceMin) {
                distanceMin = distance;
                plusProche = i;
            }
        }

        return plusProche;
    }

    /**
     * Met à jour les centroïdes en calculant le "centre" de chaque cluster.
     */
    @SuppressWarnings("unchecked")
    private T[] mettreAJourCentroides(T[] donnees, int[] affectations) {
        // Utiliser la classe du premier élément pour créer le tableau
        Class<?> componentType = donnees.getClass().getComponentType();
        T[] nouveauxCentroides = (T[]) java.lang.reflect.Array.newInstance(componentType, nbClusters);

        // Pour chaque cluster
        for (int cluster = 0; cluster < nbClusters; cluster++) {
            // Collecter tous les points du cluster
            int count = 0;
            for (int affectation : affectations) {
                if (affectation == cluster) count++;
            }

            if (count == 0) {
                // Cluster vide, réinitialiser avec un point aléatoire
                nouveauxCentroides[cluster] = donnees[random.nextInt(donnees.length)];
                continue;
            }

            // Créer un tableau avec les points du cluster
            // utilisation de Array.newInstance pour créer le tableau du bon type,
            // sans ça on peut pas vu que on utilise des types génériques
            T[] pointsCluster = (T[]) java.lang.reflect.Array.newInstance(componentType, count);
            int index = 0;
            for (int i = 0; i < affectations.length; i++) {
                if (affectations[i] == cluster) {
                    pointsCluster[index++] = donnees[i];
                }
            }

            // Calculer le centroïde
            nouveauxCentroides[cluster] = calculateurCentroide.calculerCentroide(pointsCluster);

        }

        return nouveauxCentroides;
    }
}