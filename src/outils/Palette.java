package outils;

import normeCouleurs.NormeCouleurs;
import normeCouleurs.NormeEuclidienne;

import java.awt.*;

/**
 * Représente une palette de couleurs avec une méthode de comparaison configurable.
 * Permet de trouver la couleur la plus proche d'une couleur donnée selon différentes normes.
 */
public class Palette {
    /** Tableau des couleurs de la palette */
    private Color[] couleurs;

    /** Norme utilisée pour calculer les distances entre couleurs */
    private NormeCouleurs distanceCouleur;

    /**
     * Constructeur de la palette avec norme euclidienne par défaut.
     *
     * @param couleurs Tableau des couleurs de la palette
     */
    public Palette(Color[] couleurs) {
        this.couleurs = couleurs;
        this.distanceCouleur = new NormeEuclidienne();
    }

    /**
     * Définit la norme utilisée pour comparer les couleurs.
     *
     * @param distanceCouleur La nouvelle norme à utiliser
     */
    public void setDistanceCouleur(NormeCouleurs distanceCouleur) {
        this.distanceCouleur = distanceCouleur;
    }

    /**
     * Retourne la norme actuellement utilisée.
     *
     * @return La norme courante
     */
    public NormeCouleurs getDistanceCouleur() {
        return this.distanceCouleur;
    }

    /**
     * Trouve la couleur de la palette la plus proche de la couleur donnée.
     * Utilise la norme configurée pour calculer les distances.
     *
     * @param c Couleur de référence
     * @return La couleur de la palette la plus proche
     */
    public Color getPlusProche(Color c) {
        Color plusProche = couleurs[0];
        double minDistance = distanceCouleur.distanceCouleur(c, plusProche);

        for (int i = 1; i < couleurs.length; i++) {
            double dist = distanceCouleur.distanceCouleur(c, couleurs[i]);
            if (dist < minDistance) {
                minDistance = dist;
                plusProche = couleurs[i];
            }
        }
        return plusProche;
    }

    /**
     * Retourne le nombre de couleurs dans la palette.
     *
     * @return Nombre de couleurs
     */
    public int getTaille() {
        return couleurs.length;
    }

    /**
     * Retourne une couleur à l'index donné.
     *
     * @param index Index de la couleur
     * @return La couleur à l'index donné
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public Color getCouleur(int index) {
        if (index < 0 || index >= couleurs.length) {
            throw new IndexOutOfBoundsException("Index invalide: " + index);
        }
        return couleurs[index];
    }
}