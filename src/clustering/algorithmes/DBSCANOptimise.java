package clustering.algorithmes;

import metriques.MetriqueDistance;
import outils.PixelData;
import java.util.*;
import java.awt.Color;

/**
 * Version améliorée de DBSCANOptimise qui fonctionne pour TOUS les types de métriques :
 * - Positions (x,y) avec grille 2D
 * - Couleurs (RGB) avec grille 3D
 *
 * Détecte automatiquement le type selon la métrique utilisée.
 */
public class DBSCANOptimise extends AlgorithmeClusteringAbstrait {

    private final double eps;
    private final int minPts;

    // Grille spatiale (peut être 2D ou 3D selon le cas)
    private Map<String, List<Integer>> grilleSpatiale;
    private int tailleGrille;

    // Type de métrique détecté
    private boolean estMetriqueCouleur;

    // Limites pour la grille
    private double minX, minY, minZ, maxX, maxY, maxZ;

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

        // Détecter le type de métrique
        estMetriqueCouleur = metrique.getNom().contains("RGB") ||
                metrique.getNom().contains("CIE") ||
                metrique.getNom().contains("Redmean") ||
                (metrique.getNom().contains("Euclidienne") && !metrique.getNom().contains("Position"));

        System.out.println("Type détecté: " + (estMetriqueCouleur ? "Couleur RGB" : "Position XY"));

        // Construire l'index approprié
        if (estMetriqueCouleur) {
            construireGrilleRGB(donnees);
        } else {
            construireGrilleSpatiale(donnees);
        }

        int clusterActuel = 0;

        for (int i = 0; i < n; i++) {
            if (clusters[i] != NON_VISITE) {
                continue;
            }

            // Trouver les voisins avec la grille appropriée
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
     * Construit la grille spatiale 2D pour les positions.
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

        // Ajuster la taille de grille selon la densité
        double largeur = maxX - minX + 1;
        double hauteur = maxY - minY + 1;
        double densite = donnees.length / (largeur * hauteur);

        if (densite > 0.5) {
            tailleGrille = (int) Math.max(1, eps / 2);
        } else if (densite < 0.1) {
            tailleGrille = (int) Math.ceil(eps * 2);
        }

        // Placer chaque point dans la grille
        for (int i = 0; i < donnees.length; i++) {
            PixelData pixel = donnees[i];
            String cle = getCleGrille2D(pixel.getX(), pixel.getY());
            grilleSpatiale.computeIfAbsent(cle, k -> new ArrayList<>()).add(i);
        }
    }

    /**
     * Construit la grille 3D pour les couleurs RGB.
     */
    private void construireGrilleRGB(PixelData[] donnees) {
        grilleSpatiale = new HashMap<>();

        // Pour RGB, les limites sont fixes
        minX = minY = minZ = 0;
        maxX = maxY = maxZ = 255;

        // Adapter la taille de grille selon eps
        // Pour les distances couleur, eps est typiquement entre 10 et 100
        if (eps < 20) {
            tailleGrille = 8;   // Petites cellules pour eps faible
        } else if (eps < 50) {
            tailleGrille = 16;  // Cellules moyennes
        } else {
            tailleGrille = 32;  // Grandes cellules pour eps élevé
        }

        // Placer chaque point dans la grille RGB
        for (int i = 0; i < donnees.length; i++) {
            PixelData pixel = donnees[i];
            Color c = pixel.getCouleur();
            String cle = getCleGrille3D(c.getRed(), c.getGreen(), c.getBlue());
            grilleSpatiale.computeIfAbsent(cle, k -> new ArrayList<>()).add(i);
        }

        System.out.println("Grille RGB créée: " + grilleSpatiale.size() + " cellules, taille cellule: " + tailleGrille);
    }

    /**
     * Génère une clé pour la cellule 2D (positions).
     */
    private String getCleGrille2D(double x, double y) {
        int gx = (int) ((x - minX) / tailleGrille);
        int gy = (int) ((y - minY) / tailleGrille);
        return gx + "," + gy;
    }

    /**
     * Génère une clé pour la cellule 3D (couleurs RGB).
     */
    private String getCleGrille3D(int r, int g, int b) {
        int gr = r / tailleGrille;
        int gg = g / tailleGrille;
        int gb = b / tailleGrille;
        return gr + "," + gg + "," + gb;
    }

    /**
     * Recherche de voisins optimisée (détecte automatiquement le type).
     */
    private List<Integer> trouverVoisinsOptimise(PixelData[] donnees, int pointIndex,
                                                 MetriqueDistance metrique) {
        if (estMetriqueCouleur) {
            return trouverVoisinsRGB(donnees, pointIndex, metrique);
        } else {
            return trouverVoisinsPosition(donnees, pointIndex, metrique);
        }
    }

    /**
     * Recherche de voisins pour les positions (2D).
     */
    private List<Integer> trouverVoisinsPosition(PixelData[] donnees, int pointIndex,
                                                 MetriqueDistance metrique) {
        List<Integer> voisins = new ArrayList<>();
        PixelData pixel = donnees[pointIndex];

        int gx = (int) ((pixel.getX() - minX) / tailleGrille);
        int gy = (int) ((pixel.getY() - minY) / tailleGrille);
        int rayonCellules = (int) Math.ceil(eps / tailleGrille);

        for (int dx = -rayonCellules; dx <= rayonCellules; dx++) {
            for (int dy = -rayonCellules; dy <= rayonCellules; dy++) {
                double distCellule = Math.sqrt(dx * dx + dy * dy) * tailleGrille;
                if (distCellule > eps + tailleGrille * Math.sqrt(2)) {
                    continue;
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
     * Recherche de voisins pour les couleurs RGB (3D).
     */
    private List<Integer> trouverVoisinsRGB(PixelData[] donnees, int pointIndex,
                                            MetriqueDistance metrique) {
        List<Integer> voisins = new ArrayList<>();
        PixelData pixel = donnees[pointIndex];
        Color c = pixel.getCouleur();

        int gr = c.getRed() / tailleGrille;
        int gg = c.getGreen() / tailleGrille;
        int gb = c.getBlue() / tailleGrille;

        // Pour RGB, on doit explorer un cube 3D
        int rayonCellules = (int) Math.ceil(eps / tailleGrille);

        for (int dr = -rayonCellules; dr <= rayonCellules; dr++) {
            for (int dg = -rayonCellules; dg <= rayonCellules; dg++) {
                for (int db = -rayonCellules; db <= rayonCellules; db++) {
                    // Vérification rapide avec distance euclidienne RGB
                    double distCellule = Math.sqrt(dr*dr + dg*dg + db*db) * tailleGrille;
                    if (distCellule > eps * 2) { // Marge de sécurité pour les métriques complexes
                        continue;
                    }

                    String cle = (gr + dr) + "," + (gg + dg) + "," + (gb + db);
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
        }

        return voisins;
    }

    /**
     * Étend le cluster (identique à la version originale).
     */
    private void expandCluster(PixelData[] donnees, int[] clusters, int pointIndex,
                               List<Integer> voisins, int clusterId,
                               MetriqueDistance metrique) {
        clusters[pointIndex] = clusterId;

        List<Integer> aTraiter = new ArrayList<>(voisins);
        Set<Integer> voisinsSet = new HashSet<>(voisins);

        int index = 0;
        while (index < aTraiter.size()) {
            int voisinIndex = aTraiter.get(index++);

            if (clusters[voisinIndex] == NON_VISITE) {
                clusters[voisinIndex] = clusterId;

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

    // Getters
    public double getEps() { return eps; }
    public int getMinPts() { return minPts; }
}