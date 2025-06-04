package filtres;

import java.io.IOException;

public class TestFiltres {

    public static void main(String[] args) throws IOException {

        Filtre filtreFlouMoyenne = new FiltreFlouGaussien(7,1.5);

        filtreFlouMoyenne.appliquerFiltre("./exoplanètes/Planete 1.jpg","./resultatFiltre/Planet_1_FlouGauss7x7.jpg");




    }
}
