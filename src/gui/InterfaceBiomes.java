package gui;

import biomes.AffichageBiomes;
import biomes.EtiqueteurBiomes;
import clusters.KMeans;
import clusters.VisualisationBiomes;
import filtres.FiltreFlouGaussien;
import filtres.FiltreFlouMoyenne;
import normeCouleurs.*;
import outils.OutilsImage;
import outils.Palette;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class InterfaceBiomes extends JFrame {

    // Composants GUI
    private JLabel labelImage;
    private JButton btnChargerImage;
    private JButton btnAppliquerFiltre;
    private JButton btnAnalyserBiomes;
    private JButton btnVoirBiomesIndividuels;

    // Paramètres
    private JComboBox<String> comboNormes;
    private JSpinner spinnerNbBiomes;
    private JComboBox<String> comboFiltres;
    private JSpinner spinnerTailleFiltre;
    private JSpinner spinnerSigma;

    // Affichage résultats
    private JTextArea textResultats;
    private JLabel labelTempsExecution;
    private JPanel panelImages;

    // Variables de travail
    private BufferedImage imageOriginale;
    private BufferedImage imageFiltree;
    private String cheminImageActuel;
    private int[] affectations;
    private String[] etiquettes;
    private KMeans kmeans;

    public InterfaceBiomes() {
        super("🌍 Détection de Biomes sur Exoplanètes");
        initializeGUI();
        setupEventHandlers();
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal avec scroll
        JPanel mainPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de contrôles à gauche
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.WEST);

        // Panel d'affichage au centre
        JPanel displayPanel = createDisplayPanel();
        mainPanel.add(displayPanel, BorderLayout.CENTER);

        // Panel de résultats en bas
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Section chargement image
        panel.add(createImageSection());
        panel.add(Box.createVerticalStrut(10));

        // Section filtrage
        panel.add(createFilterSection());
        panel.add(Box.createVerticalStrut(10));

        // Section clustering
        panel.add(createClusteringSection());
        panel.add(Box.createVerticalStrut(10));

        // Section actions
        panel.add(createActionSection());

        return panel;
    }

    private JPanel createImageSection() {
        JPanel section = new JPanel(new FlowLayout());
        section.setBorder(new TitledBorder("📁 Image"));

        btnChargerImage = new JButton("Charger Image");
        btnChargerImage.setPreferredSize(new Dimension(200, 30));
        section.add(btnChargerImage);

        return section;
    }

    private JPanel createFilterSection() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBorder(new TitledBorder("🔧 Filtrage"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Type de filtre
        gbc.gridx = 0; gbc.gridy = 0;
        section.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        comboFiltres = new JComboBox<>(new String[]{"Flou Gaussien", "Flou Moyenne"});
        section.add(comboFiltres, gbc);

        // Taille filtre
        gbc.gridx = 0; gbc.gridy = 1;
        section.add(new JLabel("Taille:"), gbc);
        gbc.gridx = 1;
        spinnerTailleFiltre = new JSpinner(new SpinnerNumberModel(7, 3, 15, 2));
        section.add(spinnerTailleFiltre, gbc);

        // Sigma (pour gaussien)
        gbc.gridx = 0; gbc.gridy = 2;
        section.add(new JLabel("Sigma:"), gbc);
        gbc.gridx = 1;
        spinnerSigma = new JSpinner(new SpinnerNumberModel(1.5, 0.1, 5.0, 0.1));
        section.add(spinnerSigma, gbc);

        // Bouton appliquer filtre
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnAppliquerFiltre = new JButton("Appliquer Filtre");
        btnAppliquerFiltre.setEnabled(false);
        section.add(btnAppliquerFiltre, gbc);

        return section;
    }

    private JPanel createClusteringSection() {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBorder(new TitledBorder("🎯 Clustering"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Norme de couleur
        gbc.gridx = 0; gbc.gridy = 0;
        section.add(new JLabel("Norme:"), gbc);
        gbc.gridx = 1;
        comboNormes = new JComboBox<>(new String[]{
                "CIE94", "CIELAB", "Euclidienne", "Redmean"
        });
        section.add(comboNormes, gbc);

        // Nombre de biomes
        gbc.gridx = 0; gbc.gridy = 1;
        section.add(new JLabel("Nb Biomes:"), gbc);
        gbc.gridx = 1;
        spinnerNbBiomes = new JSpinner(new SpinnerNumberModel(7, 2, 15, 1));
        section.add(spinnerNbBiomes, gbc);

        return section;
    }

    private JPanel createActionSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new TitledBorder("🚀 Actions"));

        btnAnalyserBiomes = new JButton("🔍 Analyser Biomes");
        btnAnalyserBiomes.setEnabled(false);
        btnAnalyserBiomes.setPreferredSize(new Dimension(200, 40));
        btnAnalyserBiomes.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnVoirBiomesIndividuels = new JButton("👁 Voir Biomes Individuels");
        btnVoirBiomesIndividuels.setEnabled(false);
        btnVoirBiomesIndividuels.setPreferredSize(new Dimension(200, 30));
        btnVoirBiomesIndividuels.setAlignmentX(Component.CENTER_ALIGNMENT);

        section.add(btnAnalyserBiomes);
        section.add(Box.createVerticalStrut(10));
        section.add(btnVoirBiomesIndividuels);

        return section;
    }

    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("🖼 Visualisation"));

        panelImages = new JPanel(new GridLayout(1, 3, 10, 10));
        panelImages.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Labels pour les images
        panelImages.add(createImagePanel("Image Originale", 400, 300));
        panelImages.add(createImagePanel("Image Filtrée", 400, 300));
        panelImages.add(createImagePanel("Biomes Détectés", 400, 300));

        panel.add(panelImages, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createImagePanel(String title, int width, int height) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(title));

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(BorderFactory.createLoweredBevelBorder());
        label.setText("Aucune image");

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("📊 Résultats"));
        panel.setPreferredSize(new Dimension(800, 200));

        textResultats = new JTextArea(8, 50);
        textResultats.setEditable(false);
        textResultats.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollText = new JScrollPane(textResultats);

        labelTempsExecution = new JLabel("Temps d'exécution: -");
        labelTempsExecution.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(scrollText, BorderLayout.CENTER);
        panel.add(labelTempsExecution, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEventHandlers() {
        btnChargerImage.addActionListener(e -> chargerImage());
        btnAppliquerFiltre.addActionListener(e -> appliquerFiltre());
        btnAnalyserBiomes.addActionListener(e -> analyserBiomes());
        btnVoirBiomesIndividuels.addActionListener(e -> voirBiomesIndividuels());

        // Activer/désactiver sigma selon le type de filtre
        comboFiltres.addActionListener(e -> {
            boolean isGaussien = "Flou Gaussien".equals(comboFiltres.getSelectedItem());
            spinnerSigma.setEnabled(isGaussien);
        });
    }

    private void chargerImage() {
        JFileChooser chooser = new JFileChooser("./exoplanètes");
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                cheminImageActuel = file.getAbsolutePath();
                imageOriginale = OutilsImage.convertionCheminEnBufferedImage(cheminImageActuel);

                // Afficher l'image originale
                afficherImage(imageOriginale, 0);
                btnAppliquerFiltre.setEnabled(true);

                textResultats.setText("✅ Image chargée: " + file.getName() + "\n");
                textResultats.append("Dimensions: " + imageOriginale.getWidth() + "x" + imageOriginale.getHeight() + " pixels\n\n");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void appliquerFiltre() {
        if (imageOriginale == null) return;

        try {
            long debut = System.currentTimeMillis();

            String typeFiltre = (String) comboFiltres.getSelectedItem();
            int taille = (Integer) spinnerTailleFiltre.getValue();

            // Créer fichier temporaire
            String tempFile = "./temp_filtered.jpg";
            OutilsImage.sauverImage(imageOriginale, tempFile);

            String outputFile = "./temp_result.jpg";

            if ("Flou Gaussien".equals(typeFiltre)) {
                double sigma = (Double) spinnerSigma.getValue();
                FiltreFlouGaussien filtre = new FiltreFlouGaussien(taille, sigma);
                filtre.appliquerFiltre(tempFile, outputFile);
            } else {
                FiltreFlouMoyenne filtre = new FiltreFlouMoyenne(taille);
                filtre.appliquerFiltre(tempFile, outputFile);
            }

            imageFiltree = OutilsImage.convertionCheminEnBufferedImage(outputFile);
            afficherImage(imageFiltree, 1);
            btnAnalyserBiomes.setEnabled(true);

            long temps = System.currentTimeMillis() - debut;
            textResultats.append("🔧 Filtre appliqué: " + typeFiltre + " (taille " + taille + ")\n");
            textResultats.append("⏱ Temps de filtrage: " + temps + " ms\n\n");

            // Nettoyer fichiers temporaires
            new File(tempFile).delete();
            new File(outputFile).delete();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du filtrage: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void analyserBiomes() {
        if (imageFiltree == null) return;

        try {
            long debut = System.currentTimeMillis();

            // Paramètres
            int nbBiomes = (Integer) spinnerNbBiomes.getValue();
            String normeStr = (String) comboNormes.getSelectedItem();
            NormeCouleurs norme = creerNorme(normeStr);

            // Extraction des données
            int[][] donneesRGB = OutilsImage.extraireDonneesPixels(imageFiltree);

            // Clustering
            kmeans = new KMeans(norme, nbBiomes);
            affectations = kmeans.classifier(donneesRGB);

            // Étiquetage
            etiquettes = EtiqueteurBiomes.etiqueterTousLesBiomes(kmeans.getCentroides());

            // Visualisation
            Palette paletteBiomes = kmeans.creerPaletteBiomes(kmeans.getCentroides());
            BufferedImage imageResultat = VisualisationBiomes.visualiserBiomes(
                    imageFiltree, affectations, paletteBiomes);

            afficherImage(imageResultat, 2);
            btnVoirBiomesIndividuels.setEnabled(true);

            long temps = System.currentTimeMillis() - debut;
            labelTempsExecution.setText("⏱ Temps d'exécution total: " + temps + " ms");

            // Afficher résultats détaillés
            afficherResultatsDetailles(temps, nbBiomes, normeStr);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'analyse: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirBiomesIndividuels() {
        if (kmeans == null || affectations == null) return;

        try {
            // Créer une nouvelle fenêtre pour les biomes individuels
            JFrame frameBiomes = new JFrame("🌍 Biomes Individuels Détectés");
            frameBiomes.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panelBiomes = new JPanel(new GridLayout(0, 3, 10, 10));
            panelBiomes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            for (int i = 0; i < etiquettes.length; i++) {
                BufferedImage imageBiome = AffichageBiomes.afficherBiomeIsole(
                        imageFiltree, affectations, i, etiquettes[i], 75);

                JPanel biomPanel = new JPanel(new BorderLayout());
                biomPanel.setBorder(new TitledBorder("Biome " + i + ": " + etiquettes[i]));

                ImageIcon icon = new ImageIcon(redimensionnerImage(imageBiome, 250, 200));
                JLabel labelBiome = new JLabel(icon);
                biomPanel.add(labelBiome, BorderLayout.CENTER);

                // Statistiques du biome
                int compteur = 0;
                for (int aff : affectations) {
                    if (aff == i) compteur++;
                }
                double pourcentage = (compteur * 100.0) / affectations.length;

                JLabel labelStats = new JLabel(String.format("%.1f%% (%d pixels)", pourcentage, compteur));
                labelStats.setHorizontalAlignment(JLabel.CENTER);
                biomPanel.add(labelStats, BorderLayout.SOUTH);

                panelBiomes.add(biomPanel);
            }

            JScrollPane scrollBiomes = new JScrollPane(panelBiomes);
            frameBiomes.add(scrollBiomes);
            frameBiomes.setSize(800, 600);
            frameBiomes.setLocationRelativeTo(this);
            frameBiomes.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'affichage: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private NormeCouleurs creerNorme(String nomNorme) {
        switch (nomNorme) {
            case "CIE94": return new NormeCie94();
            case "CIELAB": return new NormeCielab();
            case "Euclidienne": return new NormeEuclidienne();
            case "Redmean": return new NormeRedmean();
            default: return new NormeCie94();
        }
    }

    private void afficherImage(BufferedImage image, int position) {
        if (image == null) return;

        // Redimensionner pour l'affichage
        BufferedImage imageRedim = redimensionnerImage(image, 380, 280);
        ImageIcon icon = new ImageIcon(imageRedim);

        JPanel panel = (JPanel) panelImages.getComponent(position);
        JLabel label = (JLabel) panel.getComponent(0);
        label.setIcon(icon);
        label.setText("");

        repaint();
    }

    private BufferedImage redimensionnerImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    private void afficherResultatsDetailles(long temps, int nbBiomes, String norme) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 ANALYSE TERMINÉE\n");
        sb.append("==================\n");
        sb.append("Norme utilisée: ").append(norme).append("\n");
        sb.append("Nombre de biomes: ").append(nbBiomes).append("\n");
        sb.append("Temps total: ").append(temps).append(" ms\n\n");

        sb.append("📊 BIOMES DÉTECTÉS:\n");
        sb.append("===================\n");

        // Calculer statistiques
        int[] compteurs = new int[etiquettes.length];
        for (int aff : affectations) {
            compteurs[aff]++;
        }

        int totalPixels = affectations.length;
        for (int i = 0; i < etiquettes.length; i++) {
            double pourcentage = (compteurs[i] * 100.0) / totalPixels;
            int[] centroide = kmeans.getCentroides()[i];

            sb.append(String.format("• %s: %.1f%% (%d pixels)\n",
                    etiquettes[i], pourcentage, compteurs[i]));
            sb.append(String.format("  RGB: (%d,%d,%d)\n\n",
                    centroide[0], centroide[1], centroide[2]));
        }

        textResultats.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InterfaceBiomes().setVisible(true);
        });
    }
}