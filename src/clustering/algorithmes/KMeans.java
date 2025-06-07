package clustering.algorithmes;

import outils.PixelData;
import metriques.MetriqueDistance;
import java.awt.Color;
import java.util.Random;
import java.util.Arrays;

/**
 * Implémentation de K-Means pour PixelData.
 */
public class KMeans extends AlgorithmeClusteringAbstrait {

    private final int nbClusters;
    private final int maxIterations;
    private final Random random;

    public KMeans(int nbClusters, int maxIterations) {
        super("K-Means (K=" + nbClusters + ")");
        this.nbClusters = nbClusters;
        this.maxIterations = maxIterations;
        this.random = new Random();
    }

    @Override
    public int[] executer(PixelData[] donnees, MetriqueDistance metrique) {
        int n = donnees.length;
        if (n == 0) return new int[0];

        // Initialisation
        int[] affectations = new int[n];
        PixelData[] centroides = initialiserCentroides(donnees);

        // Boucle principale
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int[] nouvellesAffectations = new int[n];

            // Étape 1 : Affecter chaque point au centroïde le plus proche
            for (int i = 0; i < donnees.length; i++) {
                nouvellesAffectations[i] = trouverCentroideLePlusProche(donnees[i], centroides, metrique);
            }

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
    private PixelData[] initialiserCentroides(PixelData[] donnees) {
        PixelData[] centroides = new PixelData[nbClusters];
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
     * Trouve l'indice du centroïde le plus proche d'un point donné.
     */
    private int trouverCentroideLePlusProche(PixelData point, PixelData[] centroides,
                                             MetriqueDistance metrique) {
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
     * Met à jour les centroïdes en calculant le centre de chaque cluster.
     */
    private PixelData[] mettreAJourCentroides(PixelData[] donnees, int[] affectations) {
        PixelData[] nouveauxCentroides = new PixelData[nbClusters];

        for (int cluster = 0; cluster < nbClusters; cluster++) {
            double moyX = 0, moyY = 0;
            double moyR = 0, moyG = 0, moyB = 0;
            int count = 0;

            // Calculer les moyennes pour ce cluster
            for (int i = 0; i < affectations.length; i++) {
                if (affectations[i] == cluster) {
                    PixelData pixel = donnees[i];

                    // Toujours calculer les moyennes de position ET couleur
                    moyX += pixel.getX();
                    moyY += pixel.getY();

                    Color c = pixel.getCouleur();
                    moyR += c.getRed();
                    moyG += c.getGreen();
                    moyB += c.getBlue();

                    count++;
                }
            }

            if (count == 0) {
                // Cluster vide, prendre un point aléatoire
                nouveauxCentroides[cluster] = donnees[random.nextInt(donnees.length)];
            } else {
                // Créer le centroïde avec les moyennes calculées
                int x = (int) Math.round(moyX / count);
                int y = (int) Math.round(moyY / count);
                int r = clamp((int) Math.round(moyR / count));
                int g = clamp((int) Math.round(moyG / count));
                int b = clamp((int) Math.round(moyB / count));

                nouveauxCentroides[cluster] = new PixelData(x, y, new Color(r, g, b), -1);
            }
        }

        return nouveauxCentroides;
    }

    /**
     * S'assure qu'une valeur est dans l'intervalle [0, 255] pour les couleurs.
     */
    private int clamp(int valeur) {
        return Math.max(0, Math.min(255, valeur));
    }
}