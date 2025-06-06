package clustering.algorithmes;

import clustering.AlgorithmeClusteringAbstrait;
import clustering.CalculateurCentroide;
import clustering.centroides.CalculateurCentroidePixel;
import clustering.centroides.CalculateurCentroidePixel.TypeCentroide;
import donnees.PixelData;
import metriques.MetriqueDistance;

import java.awt.*;
import java.util.Random;
import java.util.Arrays;

/**
 * Implémentation générique de l'algorithme K-Means.
 * Fonctionne avec n'importe quel type de données et métrique.
 *
 * @param <T> Le type de données à clustériser
 */
public class KMeansGenerique<T> extends AlgorithmeClusteringAbstrait<T> {

    private final int k; // Nombre de clusters souhaité
    private final int maxIterations;
    private final Random random;
    private CalculateurCentroide<T> calculateurCentroide;

    // Pour le multithreading
    private final Object[] verrous; // Un verrou par centroïde pour éviter les conflits

    public KMeansGenerique(int k, int maxIterations) {
        super("K-Means (K=" + k + ")");
        this.k = k;
        this.maxIterations = maxIterations;
        this.random = new Random();
        this.nombreClusters = k;

        // Initialiser les verrous pour le multithreading
        this.verrous = new Object[k];
        for (int i = 0; i < k; i++) {
            verrous[i] = new Object();
        }
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

        return affectations;
    }

    /**
     * Initialise les centroïdes en choisissant k points aléatoires.
     */
    @SuppressWarnings("unchecked")
    private T[] initialiserCentroides(T[] donnees) {
        Class<?> componentType = donnees.getClass().getComponentType();
        T[] centroides = (T[]) java.lang.reflect.Array.newInstance(componentType, k);
        boolean[] choisis = new boolean[donnees.length];

        for (int i = 0; i < k; i++) {
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
     * Affecte chaque point au centroïde le plus proche (version séquentielle).
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
        T[] nouveauxCentroides = (T[]) java.lang.reflect.Array.newInstance(componentType, k);

        // Pour chaque cluster
        for (int cluster = 0; cluster < k; cluster++) {
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
            // Utiliser Array.newInstance pour créer le tableau du bon type
            T[] pointsCluster = (T[]) java.lang.reflect.Array.newInstance(componentType, count);
            int index = 0;
            for (int i = 0; i < affectations.length; i++) {
                if (affectations[i] == cluster) {
                    pointsCluster[index++] = donnees[i];
                }
            }

            // Calculer le centroïde
            if (calculateurCentroide != null && calculateurCentroide.peutCreerNouveauxObjets()) {
                // Utiliser le calculateur pour créer un vrai centroïde
                nouveauxCentroides[cluster] = calculateurCentroide.calculerCentroide(pointsCluster);
            } else {
                // Fallback : prendre le point le plus central du cluster
                nouveauxCentroides[cluster] = trouverPointLePlusCentral(pointsCluster);
            }
        }

        return nouveauxCentroides;
    }

    /**
     * Trouve le point le plus central d'un ensemble (médoïde).
     * Utilisé quand on ne peut pas calculer un vrai centroïde.
     */
    private T trouverPointLePlusCentral(T[] points) {
        if (points.length == 1) return points[0];

        T pointCentral = points[0];
        double distanceMinTotale = Double.MAX_VALUE;

        // Pour chaque point candidat
        for (T candidat : points) {
            double distanceTotale = 0;

            // Calculer la somme des distances aux autres points
            for (T autre : points) {
                distanceTotale += Math.pow(distanceEuclidienne(candidat, autre), 2);
            }

            // Si ce point est plus central, le garder
            if (distanceTotale < distanceMinTotale) {
                distanceMinTotale = distanceTotale;
                pointCentral = candidat;
            }
        }

        return pointCentral;
    }

    /**
     * Calcule une distance euclidienne générique entre deux objets.
     * Utilisée pour trouver le médoïde quand on n'a pas de calculateur.
     */
    private double distanceEuclidienne(T obj1, T obj2) {
        // Si ce sont des PixelData, on peut calculer directement
        if (obj1 instanceof PixelData && obj2 instanceof PixelData) {
            PixelData p1 = (PixelData) obj1;
            PixelData p2 = (PixelData) obj2;

            double dx = p1.getX() - p2.getX();
            double dy = p1.getY() - p2.getY();

            Color c1 = p1.getCouleur();
            Color c2 = p2.getCouleur();
            double dr = c1.getRed() - c2.getRed();
            double dg = c1.getGreen() - c2.getGreen();
            double db = c1.getBlue() - c2.getBlue();

            // Normaliser les distances couleur et position
            return Math.sqrt((dx*dx + dy*dy) / 1000.0 + (dr*dr + dg*dg + db*db) / 100.0);
        }

        // Sinon, on ne peut pas calculer
        return 0;
    }
}