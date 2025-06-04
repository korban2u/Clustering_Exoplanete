package filtres;

import java.io.IOException;

/**
 * Interface représentant un filtre d'image.
 */
public interface Filtre {

    /**
     * Applique le filtre à une image source et enregistre le résultat à l'emplacement de destination.
     * @param cheminSource      Chemin du fichier image source.
     * @param cheminDestination Chemin du fichier où l'image filtrée sera enregistrée.
     * @throws IOException En cas d'erreur lors de la lecture ou de l'écriture de l'image.
     */
    public void appliquerFiltre(String cheminSource, String cheminDestination) throws IOException;

    /**
     * Retourne le nom du filtre.
     * @return Nom du filtre.
     */
    public String getNomFiltre();
}
