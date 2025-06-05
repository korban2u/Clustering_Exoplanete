package clusters;

import outils.Palette;
import normeCouleurs.NormeCie94;
import normeCouleurs.NormeCouleurs;
import outils.OutilsImage;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static outils.OutilsImage.extraireDonneesPixels;

public class MainClustering {

    public static void main(String[] args) throws IOException {

        afficherCluster("./resultatFiltre/Planet_1_FlouGauss7x7.jpg",7);

    }

    public static void afficherCluster(String cheminImage, int nbBiomes) throws IOException {
        // 1. Charger l'image
        BufferedImage image =  OutilsImage.convertionCheminEnBufferedImage(cheminImage);

        // 2. Extraire les données RGB
        int[][] donneesRGB = extraireDonneesPixels(image);

        // 3. Clustering K-Means
        NormeCouleurs NormeCie94 = new NormeCie94();
        KMeans kmeans = new KMeans(NormeCie94,nbBiomes);
        int[] affectations = kmeans.classifier(donneesRGB);

        // 4. Créer palette des biomes trouvés
        Palette paletteBiomes = kmeans.creerPaletteBiomes(kmeans.getCentroides());

        // 5. Visualiser le résultat
        BufferedImage imageResultat = VisualisationBiomes.visualiserBiomes(
                image, affectations, paletteBiomes);

        // 6. Sauvegarder
        OutilsImage.sauverImage(imageResultat,"./imagesCopie/biomes_detectes.jpg");
    }
}