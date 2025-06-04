package clusters;

import outils.Palette;
import normeCouleurs.NormeCouleurs;
import outils.OutilCouleur;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

public class KMeans implements AlgoClustering{

    private NormeCouleurs norme;
    private int[][] centroides;
    public KMeans(NormeCouleurs norme) {
        this.norme = norme;
    }

    @Override
    public int[] classifier(int[][] donnees, int nbClusters) {
        centroides = iniCentroides(donnees,nbClusters);

        boolean fin = false;

        // initialiser les groupes
        int[] affectationGroupes = new int[donnees.length];
        while (!fin){
            int[] nouvelleAffectation = new int[donnees.length];
            for (int i = 0;i<donnees.length;i++){
                int[] couleurPixel = donnees[i];
                int biomePlusProche = trouverBiomePlusProche(couleurPixel,centroides);
                nouvelleAffectation[i] = biomePlusProche;
            }

            fin = Arrays.equals(affectationGroupes,nouvelleAffectation);
            affectationGroupes = nouvelleAffectation;

            if(!fin){
                centroides = majCentroides(donnees, affectationGroupes, nbClusters);
            }

        }
        return affectationGroupes;
    }

    private int trouverBiomePlusProche(int[] couleurPixel, int[][] centroides) {
        int plusProche = 0;
        double distanceMin = norme.distanceCouleur(
                OutilCouleur.getColor(couleurPixel),
                OutilCouleur.getColor(centroides[plusProche])
        );
        for (int i =1 ;i<centroides.length;i++){
            double distance = norme.distanceCouleur(
                    OutilCouleur.getColor(couleurPixel),
                    OutilCouleur.getColor(centroides[i])
            );
            if(distance < distanceMin){
                distanceMin = distance;
                plusProche = i;
            }
        }
        return plusProche;
    }

    private int[][] majCentroides(int[][] donnees, int[] affectationGroupes, int nbClusters) {
        int[][] majCentroides = new int[nbClusters][3];
        int[] nbPointsCentroides = new int[nbClusters];

        // Accumulation
        for (int i = 0; i < donnees.length; i++) {
            int cluster = affectationGroupes[i];

            nbPointsCentroides[cluster]++;
            majCentroides[cluster][0] += donnees[i][0];
            majCentroides[cluster][1] += donnees[i][1];
            majCentroides[cluster][2] += donnees[i][2];
        }

        // Moyennes avec gestion des clusters vides
        for (int i = 0; i < nbClusters; i++) {
            if (nbPointsCentroides[i] > 0) {
                majCentroides[i][0] /= nbPointsCentroides[i];
                majCentroides[i][1] /= nbPointsCentroides[i];
                majCentroides[i][2] /= nbPointsCentroides[i];
            } else {
                // Réinitialiser avec un point aléatoire
                Random random = new Random();
                int indexAleatoire = random.nextInt(donnees.length);
                majCentroides[i] = donnees[indexAleatoire].clone();
            }
        }

        return majCentroides;
    }


    // Initialiser centroïdes (couleurs représentatives)
    private int[][] iniCentroides(int[][] donnes, int nbClusters){
        Random random = new Random();
        int[][] centroides = new int[nbClusters][3];
        for (int i = 0; i < nbClusters; i++) {
            // Choisir un pixel aléatoire comme centroïde initial
            int indexAleatoire = random.nextInt(donnes.length);
            centroides[i] = donnes[indexAleatoire].clone();
        }
        return centroides;
    }

    public Palette creerPaletteBiomes(int[][] centroides) {
        Color[] couleursBiomes = new Color[centroides.length];

        for (int i = 0; i < centroides.length; i++) {
            int r = centroides[i][0];
            int g = centroides[i][1];
            int b = centroides[i][2];
            couleursBiomes[i] = new Color(r, g, b);
        }

        return new Palette(couleursBiomes);
    }

    public int[][] getCentroides() {
        return centroides;
    }



}
