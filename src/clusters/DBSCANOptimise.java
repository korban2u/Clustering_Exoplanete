package clusters;

import java.util.*;

/**
 * Version optimisée de DBSCAN pour la détection d'écosystèmes.
 * Utilise un index spatial et les informations de biomes pour accélérer les calculs.
 */
public class DBSCANOptimise implements AlgoClustering {

    private double eps;
    private int minPts;
    private int nombreClusters;

    // Index spatial pour accélération
    private Map<String, List<Integer>> grilleSpatiale;
    private int tailleGrille;

    // États des points
    private static final int NON_VISITE = 0;
    private static final int BRUIT = -1;

    public DBSCANOptimise(double eps, int minPts) {
        this.eps = eps;
        this.minPts = minPts;
        // La taille de la grille est basée sur eps pour optimiser les recherches
        this.tailleGrille = (int) Math.ceil(eps);
    }

    @Override
    public int[] classifier(int[][] objets) {
        int n = objets.length;
        int[] clusters = new int[n];
        Arrays.fill(clusters, NON_VISITE);

        // Construire l'index spatial
        construireGrilleSpatiale(objets);

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Utiliser la recherche optimisée
            List<Integer> voisins = regionQueryOptimisee(objets, i);

            if (voisins.size() < minPts) {
                clusters[i] = BRUIT;
            } else {
                expandClusterOptimise(objets, clusters, i, voisins, clusterActuel);
                clusterActuel++;
            }
        }

        this.nombreClusters = clusterActuel;

        // Gérer les points de bruit
        for (int i = 0; i < n; i++) {
            if (clusters[i] == BRUIT) {
                clusters[i] = -1;
            }
        }

        return clusters;
    }

    /**
     * Construit une grille spatiale pour accélérer les recherches de voisins
     */
    private void construireGrilleSpatiale(int[][] objets) {
        grilleSpatiale = new HashMap<>();

        for (int i = 0; i < objets.length; i++) {
            String cle = getCleGrille(objets[i][0], objets[i][1]);
            grilleSpatiale.computeIfAbsent(cle, k -> new ArrayList<>()).add(i);
        }
    }

    /**
     * Génère une clé pour la cellule de grille contenant le point (x, y)
     */
    private String getCleGrille(int x, int y) {
        int gx = x / tailleGrille;
        int gy = y / tailleGrille;
        return gx + "," + gy;
    }

    /**
     * Version optimisée de regionQuery utilisant l'index spatial
     */
    List<Integer> regionQueryOptimisee(int[][] objets, int pointIndex) {
        List<Integer> voisins = new ArrayList<>();
        int[] point = objets[pointIndex];

        // Calculer les cellules de grille à vérifier
        int gx = point[0] / tailleGrille;
        int gy = point[1] / tailleGrille;
        int rayonGrille = (int) Math.ceil(eps / tailleGrille);

        // Vérifier uniquement les cellules voisines pertinentes
        for (int dx = -rayonGrille; dx <= rayonGrille; dx++) {
            for (int dy = -rayonGrille; dy <= rayonGrille; dy++) {
                String cle = (gx + dx) + "," + (gy + dy);
                List<Integer> pointsDansCellule = grilleSpatiale.get(cle);

                if (pointsDansCellule != null) {
                    for (int i : pointsDansCellule) {
                        if (distanceEuclidienne(point, objets[i]) <= eps) {
                            voisins.add(i);
                        }
                    }
                }
            }
        }

        return voisins;
    }

    /**
     * Version optimisée d'expandCluster
     */
    private void expandClusterOptimise(int[][] objets, int[] clusters, int pointIndex,
                                       List<Integer> voisins, int clusterId) {
        clusters[pointIndex] = clusterId;

        // Utiliser une liste au lieu d'une queue pour un accès plus rapide
        List<Integer> aTraiter = new ArrayList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        int index = 0;
        while (index < aTraiter.size()) {
            int voisinIndex = aTraiter.get(index++);

            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

                List<Integer> voisinsDuVoisin = regionQueryOptimisee(objets, voisinIndex);

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

    private double distanceEuclidienne(int[] p1, int[] p2) {
        double dx = p1[0] - p2[0];
        double dy = p1[1] - p2[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public int getNombreClusters() {
        return nombreClusters;
    }

    @Override
    public String getNomAlgorithme() {
        return "DBSCAN Optimisé";
    }

    // Getters et setters
    public double getEps() { return eps; }
    public void setEps(double eps) { this.eps = eps; }
    public int getMinPts() { return minPts; }
    public void setMinPts(int minPts) { this.minPts = minPts; }
}

