package filtres;

import static java.lang.Math.PI;
import static java.lang.Math.exp;

/**
 * Implémentation du filtre flou gaussien.
 */
public class FiltreFlouGaussien extends FiltreFlou {

    private double sigma;

    /**
     * Constructeur
     * @param tailleFiltre Taille du filtre (doit être un impair et >= 3).
     * @param sigma        Écart-type pour la fonction de Gauss.
     */
    public FiltreFlouGaussien(int tailleFiltre, double sigma) {
        super(tailleFiltre);
        this.sigma = sigma;
    }

    @Override
    public String getNomFiltre() {
        return "Flou Gaussien";
    }

    @Override
    protected double[][] calculerCoef() {
        double[][] matrice = new double[tailleFiltre][tailleFiltre];
        double somme = 0.0;
        int milieu = tailleFiltre / 2;

        // boucle sur presque tout les pixels,on ne prends pas les coins (car ils ont pas de pixel voisin)
        // la taille des coins depends de la taille du filtre, par ex : pour 3x3 on commence à (1,1)
        for (int y = -milieu; y <= milieu; y++) {
            for (int x = -milieu; x <= milieu; x++) {
                double coeff = (1.0 / (2 * PI * sigma * sigma)) * exp(-(x * x + y * y) / (2 * sigma * sigma)); // formule de Gauss
                matrice[y + milieu][x + milieu] = coeff;
                somme += coeff;
            }
        }

        // Normalisation
        // permet que la somme des coefficients du filtre = 1
        for (int i = 0; i < tailleFiltre; i++) {
            for (int j = 0; j < tailleFiltre; j++) {
                matrice[i][j] /= somme;
            }
        }

        return matrice;
    }
}
