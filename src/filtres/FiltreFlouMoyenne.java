package filtres;

/**
 * Implémentation du filtre flou par moyenne
 */
public class FiltreFlouMoyenne extends FiltreFlou {

    /**
     * Constructeur du filtre moyenne.
     *
     * @param tailleFiltre Taille du filtre (doit être un impair et >= 3)
     */
    public FiltreFlouMoyenne(int tailleFiltre) {
        super(tailleFiltre);
    }

    @Override
    public String getNomFiltre() {
        return "Flou par Moyenne";
    }

    @Override
    protected double[][] calculerCoef() {
        // ici on cree une matrice pour que tout les algo de type de flou
        //soient compatible avec la Classe Filtre flou abstrait
        double[][] matrice = new double[tailleFiltre][tailleFiltre];
        double valeur = 1.0 / (tailleFiltre * tailleFiltre);

        for (int i = 0; i < tailleFiltre; i++) {
            for (int j = 0; j < tailleFiltre; j++) {
                matrice[i][j] = valeur; // car en réalité c'est la meme valeur partout
            }
        }

        return matrice;
    }
}
