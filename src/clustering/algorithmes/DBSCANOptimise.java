package clustering.algorithmes;

import metriques.MetriqueDistance;
import outils.PixelData;
import java.util.*;

/**
 * Version optimisée de DBSCAN avec utilisation de grille spatiale.
 * Optimisé spécifiquement pour les PixelData avec coordonnées.
 */
public class DBSCANOptimise extends AlgorithmeClusteringAbstrait {

    private final double eps;
    private final int minPts;

    // Grille spatiale
    private Map<String, List<Integer>> grilleSpatiale;
    private int tailleGrille;

    // Limites spatiales pour la grille
    private double minX, minY, maxX, maxY;

    // États des points
    private static final int NON_VISITE = -2;
    private static final int BRUIT = -1;

    public DBSCANOptimise(double eps, int minPts) {
        super("DBSCAN Optimisé (eps=" + eps + ", minPts=" + minPts + ")");
        this.eps = eps;
        this.minPts = minPts;
        this.tailleGrille = (int) Math.ceil(eps);
    }

    @Override
    public int[] executer(PixelData[] donnees, MetriqueDistance metrique) {
        int n = donnees.length;
        int[] clusters = new int[n];
        Arrays.fill(clusters, NON_VISITE);

        // Construire l'index spatial
        construireGrilleSpatiale(donnees);

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Trouver les voisins avec la grille spatiale
            List<Integer> voisins = trouverVoisinsOptimise(donnees, i, metrique);

            if (voisins.size() < minPts) {
                clusters[i] = BRUIT;
            } else {
                expandCluster(donnees, clusters, i, voisins, clusterActuel, metrique);
                clusterActuel++;
            }
        }

        this.nombreClusters = clusterActuel;

        // Convertir les points de bruit
        for (int i = 0; i < n; i++) {
            if (clusters[i] == BRUIT) {
                clusters[i] = -1;
            }
        }

        // Libérer la mémoire
        grilleSpatiale.clear();

        return clusters;
    }

    /**
     * Construit la grille spatiale pour accélérer les recherches.
     */
    private void construireGrilleSpatiale(PixelData[] donnees) {
        grilleSpatiale = new HashMap<>();

        // Trouver les limites spatiales
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;

        for (PixelData pixel : donnees) {
            minX = Math.min(minX, pixel.getX());
            minY = Math.min(minY, pixel.getY());
            maxX = Math.max(maxX, pixel.getX());
            maxY = Math.max(maxY, pixel.getY());
        }

        // Ajuster la taille de la grille en fonction de la densité
        double largeur = maxX - minX + 1;
        double hauteur = maxY - minY + 1;
        double densite = donnees.length / (largeur * hauteur);

        // Adapter la taille de grille selon la densité
        if (densite > 0.5) {
            tailleGrille = (int) Math.max(1, eps / 2);
        } else if (densite < 0.1) {
            tailleGrille = (int) Math.ceil(eps * 2);
        }

        // Placer chaque point dans la grille
        for (int i = 0; i < donnees.length; i++) {
            PixelData pixel = donnees[i];
            String cle = getCleGrille(pixel.getX(), pixel.getY());
            grilleSpatiale.computeIfAbsent(cle, k -> new ArrayList<>()).add(i);
        }
    }

    /**
     * Génère une clé pour la cellule de grille.
     */
    private String getCleGrille(int x, int y) {
        int gx = (int) ((x - minX) / tailleGrille);
        int gy = (int) ((y - minY) / tailleGrille);
        return gx + "," + gy;
    }

    /**
     * Recherche de voisins optimisée avec l'index spatial.
     */
    private List<Integer> trouverVoisinsOptimise(PixelData[] donnees, int pointIndex,
                                                 MetriqueDistance metrique) {
        List<Integer> voisins = new ArrayList<>();

        PixelData pixel = donnees[pointIndex];
        int gx = (int) ((pixel.getX() - minX) / tailleGrille);
        int gy = (int) ((pixel.getY() - minY) / tailleGrille);

        // Calculer le rayon de recherche en cellules
        int rayonCellules = (int) Math.ceil(eps / tailleGrille);

        // Parcourir uniquement les cellules pertinentes
        for (int dx = -rayonCellules; dx <= rayonCellules; dx++) {
            for (int dy = -rayonCellules; dy <= rayonCellules; dy++) {
                // Vérification rapide : la cellule est-elle dans le rayon ?
                double distCellule = Math.sqrt(dx * dx + dy * dy) * tailleGrille;
                if (distCellule > eps + tailleGrille * Math.sqrt(2)) {
                    continue; // Cette cellule est trop loin
                }

                String cle = (gx + dx) + "," + (gy + dy);
                List<Integer> pointsDansCellule = grilleSpatiale.get(cle);

                if (pointsDansCellule != null) {
                    for (int i : pointsDansCellule) {
                        double distance = metrique.calculerDistance(donnees[pointIndex], donnees[i]);
                        if (distance <= eps) {
                            voisins.add(i);
                        }
                    }
                }
            }
        }

        return voisins;
    }

    /**
     * Étend le cluster en ajoutant tous les points atteignables.
     */
    private void expandCluster(PixelData[] donnees, int[] clusters, int pointIndex,
                               List<Integer> voisins, int clusterId,
                               MetriqueDistance metrique) {
        clusters[pointIndex] = clusterId;

        // Utiliser une liste au lieu d'une queue pour de meilleures performances
        List<Integer> aTraiter = new ArrayList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        int index = 0;
        while (index < aTraiter.size()) {
            int voisinIndex = aTraiter.get(index++);

            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

                // Trouver les voisins du voisin
                List<Integer> voisinsDuVoisin = trouverVoisinsOptimise(donnees, voisinIndex, metrique);

                if (voisinsDuVoisin.size() >= minPts) {
                    for (int nouveauVoisin : voisinsDuVoisin) {
                        if (!voisinsSet.contains(nouveauVoisin)) {
                            aTraiter.add(nouveauVoisin);
                            voisinsSet.add(nouveauVoisin);
                        }
                    }
                }
            } else if (clusters[voisinIndex] == BRUIT) {
                clusters[voisinIndex] = clusterId;
            }
        }
    }

    // Getters pour les paramètres
    public double getEps() {
        return eps;
    }

    public int getMinPts() {
        return minPts;
    }
}