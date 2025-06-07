import clustering.ClusteringManager;
import clustering.ClusteringManager.*;
import clustering.algorithmes.AlgorithmeClustering;
import filtres.*;
import outils.OutilsImage;
import outils.PixelData;
import validation.DaviesBouldinIndex;
import validation.SilhouetteScore;
import visualisation.VisualisationBiomes;
import visualisation.VisualisationEcosystemes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interface graphique optimisée pour l'analyse d'exoplanètes
 */
public class MainInterface extends JFrame {

    // Composants principaux
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final Map<String, JPanel> panels = new HashMap<>();
    private final Map<String, JProgressBar> progressBars = new HashMap<>();

    // Données
    private BufferedImage imageOriginale, imageFiltree;
    private String cheminImageCourante;
    private ResultatClustering resultatBiomes;
    private final List<ResultatClustering> resultatsEcosystemes = new ArrayList<>();
    private String[] etiquettesBiomes;
    private int biomeSelectionne = -1;

    // Managers
    private final ClusteringManager manager = new ClusteringManager();
    private final VisualisationBiomes visuBiomes = new VisualisationBiomes();
    private final VisualisationEcosystemes visuEcosystemes = new VisualisationEcosystemes();
    private final DaviesBouldinIndex daviesBouldin = new DaviesBouldinIndex();
    private final SilhouetteScore silhouetteScore = new SilhouetteScore();

    // Composants réutilisables
    private final Map<String, JComponent> components = new HashMap<>();

    public MainInterface() {
        super("Analyseur d'Exoplanètes");
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Créer les onglets
        createTab("Accueil", this::createAccueilPanel);
        createTab("Filtre", this::createFiltrePanel);
        createTab("Biomes", this::createBiomesPanel);
        createTab("Écosystèmes", this::createEcosystemesPanel);
        createTab("Export", this::createExportPanel);

        // Désactiver les onglets sauf accueil
        for (int i = 1; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, false);
        }

        add(tabbedPane);
        setLocationRelativeTo(null);
    }

    // === CRÉATION DES ONGLETS ===

    private void createTab(String name, Consumer<JPanel> panelCreator) {
        JPanel panel = new JPanel(new BorderLayout());
        panels.put(name, panel);

        // Créer la barre de progression
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBars.put(name, progressBar);

        // Créer le contenu
        panelCreator.accept(panel);

        // Ajouter la barre de progression si nécessaire
        if (!name.equals("Accueil")) {
            panel.add(createProgressPanel(progressBar), BorderLayout.SOUTH);
        }

        tabbedPane.addTab(name, panel);
    }

    private void createAccueilPanel(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Titre
        panel.add(createLabel("Analyseur d'Exoplanètes", 28, Font.BOLD), BorderLayout.NORTH);

        // Zone centrale
        JPanel centre = new JPanel(new BorderLayout());

        // Chargement d'image
        JPanel loadPanel = createFlowPanel(
                new JLabel("Image : "),
                components.computeIfAbsent("fieldPath", k -> createTextField(50, false)),
                components.computeIfAbsent("btnLoad", k -> createButton("Charger une image", this::chargerImage))
        );
        centre.add(loadPanel, BorderLayout.NORTH);

        // Affichage image
        JLabel labelImage = createImageLabel("Aucune image chargée", 800, 600);
        components.put("imageOriginale", labelImage);
        centre.add(new JScrollPane(labelImage), BorderLayout.CENTER);

        panel.add(centre, BorderLayout.CENTER);

        // Instructions
        String instructions = "Instructions :\n" +
                "1. Chargez une image\n" +
                "2. Appliquez un filtre\n" +
                "3. Détectez les biomes\n" +
                "4. Analysez les écosystèmes\n" +
                "5. Exportez les résultats";
        panel.add(createTextArea(instructions, false), BorderLayout.SOUTH);
    }

    private void createFiltrePanel(JPanel panel) {
        // Configuration
        Object[][] filtreConfig = {
                {"Type de filtre:", createCombo("Flou Gaussien", "Flou Moyenne")},
                {"Taille:", createSpinner(5, 3, 15, 2)},
                {"Sigma:", createSpinner(1.5, 0.5, 5.0, 0.5)}
        };

        JPanel config = createConfigPanel("Configuration du Filtre", filtreConfig);
        JButton btnApply = createButton("Appliquer le filtre", this::appliquerFiltre);
        btnApply.setEnabled(false);
        components.put("btnApplyFilter", btnApply);
        config.add(btnApply);

        panel.add(config, BorderLayout.NORTH);

        // Comparaison avant/après
        JPanel comparison = new JPanel(new GridLayout(1, 2, 10, 10));
        comparison.setBorder(BorderFactory.createTitledBorder("Comparaison"));
        components.put("comparison", comparison);
        panel.add(new JScrollPane(comparison), BorderLayout.CENTER);
    }

    private void createBiomesPanel(JPanel panel) {
        // Configuration
        JSpinner spinnerBiomes = createSpinner(6, 2, 15, 1);
        JComboBox<String> algoCombo = createCombo("K-Means", "DBSCAN", "DBSCAN Optimisé");  // AJOUT
        JComboBox<String> metricCombo = createCombo("CIELAB", "CIE94", "Euclidienne", "Redmean");

        // Paramètres DBSCAN pour biomes
        JSpinner spinnerEpsBiomes = createSpinner(20.0, 5.0, 100.0, 5.0);
        JSpinner spinnerMinPtsBiomes = createSpinner(50, 10, 200, 10);

        Object[][] biomesConfig = {
                {"Nombre de biomes:", spinnerBiomes},
                {"Epsilon (DBSCAN):", spinnerEpsBiomes},
                {"MinPts (DBSCAN):", spinnerMinPtsBiomes},
                {"Algorithme:", algoCombo},
                {"Métrique:", metricCombo}
        };

        JPanel config = createConfigPanel("Détection des Biomes", biomesConfig);

        // Gestion visibilité selon algorithme
        Runnable updateVisibility = () -> {
            boolean isKMeans = algoCombo.getSelectedIndex() == 0;
            spinnerBiomes.setVisible(isKMeans);
            ((JLabel)config.getComponent(0)).setVisible(isKMeans); // Label "Nombre de biomes"
            spinnerEpsBiomes.setVisible(!isKMeans);
            ((JLabel)config.getComponent(2)).setVisible(!isKMeans); // Label "Epsilon"
            spinnerMinPtsBiomes.setVisible(!isKMeans);
            ((JLabel)config.getComponent(4)).setVisible(!isKMeans); // Label "MinPts"
            config.revalidate();
            config.repaint();
        };

        algoCombo.addActionListener(e -> updateVisibility.run());
        updateVisibility.run(); // Appliquer visibilité initiale

        JButton btnDetect = createButton("Détecter les biomes", e -> detecterBiomes(
                algoCombo.getSelectedIndex(),
                (Integer)spinnerBiomes.getValue(),
                (Double)spinnerEpsBiomes.getValue(),
                (Integer)spinnerMinPtsBiomes.getValue(),
                metricCombo.getSelectedIndex()
        ));

        btnDetect.setEnabled(false);
        components.put("btnDetectBiomes", btnDetect);
        config.add(btnDetect);

        // Sélection biome - créer un combo vide manuellement
        JComboBox<String> comboBiome = new JComboBox<>();
        comboBiome.setEnabled(false);
        components.put("comboBiome", comboBiome);

        JButton btnShow = createButton("Afficher", this::afficherBiome);
        btnShow.setEnabled(false);
        components.put("btnShowBiome", btnShow);

        JPanel selection = createFlowPanel(
                new JLabel("Biome:"),
                comboBiome,
                btnShow
        );
        config.add(selection);

        panel.add(config, BorderLayout.NORTH);

        // Visualisation
        JPanel visu = new JPanel(new GridLayout(1, 2, 10, 10));
        JLabel globalLabel = createImageLabel("", 600, 400);
        JLabel biomeLabel = createImageLabel("", 600, 400);

        visu.add(createTitledPanel("Vue Globale", globalLabel));
        visu.add(createTitledPanel("Biome Sélectionné", biomeLabel));

        components.put("biomesGlobalLabel", globalLabel);
        components.put("biomesSelectedLabel", biomeLabel);
        components.put("biomesVisu", visu);

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        components.put("biomesStats", stats);

        JPanel center = new JPanel(new BorderLayout());
        center.add(visu, BorderLayout.CENTER);
        center.add(new JScrollPane(stats), BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);

        // Stocker les références
        components.put("spinnerBiomes", spinnerBiomes);
        components.put("spinnerEpsBiomes", spinnerEpsBiomes);
        components.put("spinnerMinPtsBiomes", spinnerMinPtsBiomes);
        components.put("algoBiomes", algoCombo);
        components.put("metricBiomes", metricCombo);
    }

    private void createEcosystemesPanel(JPanel panel) {
        // Configuration avec sélection d'algorithme
        JComboBox<String> algoCombo = createCombo("DBSCAN Optimisé", "K-Means", "DBSCAN");
        JSpinner spinnerK = createSpinner(3, 2, 10, 1);
        JSpinner spinnerEps = createSpinner(50.0, 10.0, 200.0, 10.0);
        JSpinner spinnerMinPts = createSpinner(30, 5, 100, 5);

        // Sélection du biome à analyser
        JComboBox<String> comboBiomeSelect = new JComboBox<>();
        comboBiomeSelect.setEnabled(false);
        components.put("comboBiomeEco", comboBiomeSelect);

        // Sélection du biome à visualiser (après analyse)
        JComboBox<String> comboBiomeVisu = new JComboBox<>();
        comboBiomeVisu.setEnabled(false);
        components.put("comboBiomeVisu", comboBiomeVisu);

        JButton btnShowBiome = createButton("Afficher ce biome", e -> afficherBiomeAvecEcosystemes());
        btnShowBiome.setEnabled(false);
        components.put("btnShowBiomeEco", btnShowBiome);

        Object[][] ecoConfig = {
                {"Biome à analyser:", comboBiomeSelect},
                {"Algorithme:", algoCombo},
                {"Clusters (K-Means):", spinnerK},
                {"Epsilon (DBSCAN):", spinnerEps},
                {"MinPts (DBSCAN):", spinnerMinPts}
        };

        JPanel config = createConfigPanel("Détection des Écosystèmes", ecoConfig);

        // Gestion visibilité dynamique
        algoCombo.addActionListener(e -> {
            boolean isKMeans = algoCombo.getSelectedIndex() == 1;
            boolean isDBSCAN = algoCombo.getSelectedIndex() != 1;
            spinnerK.setVisible(isKMeans);
            spinnerEps.setVisible(isDBSCAN);
            spinnerMinPts.setVisible(isDBSCAN);
            config.revalidate();
        });

        // Appliquer la visibilité initiale
        spinnerK.setVisible(false);

        // Listener pour sélection de biome
        comboBiomeSelect.addActionListener(e -> {
            if (comboBiomeSelect.getSelectedItem() != null) {
                String selection = (String)comboBiomeSelect.getSelectedItem();
                biomeSelectionne = Integer.parseInt(selection.split(":")[0].replace("Biome ", ""));
            }
        });

        // Boutons d'analyse
        JButton btnDetectEco = createButton("Analyser ce biome", e -> detecterEcosystemes(false));
        btnDetectEco.setEnabled(false);
        components.put("btnDetectEco", btnDetectEco);

        JButton btnDetectAllEco = createButton("Analyser tous les biomes", e -> detecterEcosystemes(true));
        btnDetectAllEco.setEnabled(false);
        components.put("btnDetectAllEco", btnDetectAllEco);

        JPanel buttons = createFlowPanel(btnDetectEco, btnDetectAllEco);
        config.add(buttons);

        // Panel pour visualisation après analyse
        JPanel panelVisu = new JPanel();
        panelVisu.setBorder(BorderFactory.createTitledBorder("Visualisation"));
        panelVisu.add(new JLabel("Biome à afficher:"));
        panelVisu.add(comboBiomeVisu);
        panelVisu.add(btnShowBiome);
        config.add(panelVisu);

        panel.add(config, BorderLayout.NORTH);

        // Visualisation
        JPanel visu = new JPanel(new GridLayout(1, 2));
        JLabel ecoLabel = createImageLabel("Écosystèmes", 800, 500);
        components.put("ecoLabel", ecoLabel);
        visu.add(ecoLabel);

        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
        components.put("ecoStats", stats);
        visu.add(new JScrollPane(stats));

        components.put("ecoVisu", visu);
        panel.add(visu, BorderLayout.CENTER);

        // Stocker les références
        components.put("algoEco", algoCombo);
        components.put("spinnerK", spinnerK);
        components.put("spinnerEps", spinnerEps);
        components.put("spinnerMinPts", spinnerMinPts);
    }

    private void createExportPanel(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel buttons = new JPanel(new GridLayout(3, 1, 20, 20));
        buttons.add(createButton("Exporter tout", e -> exporter("tous")));
        buttons.add(createButton("Exporter biomes", e -> exporter("biomes")));
        buttons.add(createButton("Exporter écosystèmes", e -> exporter("ecosystemes")));

        panel.add(createLabel("Export des Résultats", 24, Font.BOLD), BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
    }

    // === ACTIONS ===

    private void chargerImage(Object e) {
        JFileChooser chooser = new JFileChooser("./exoplanètes");
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            cheminImageCourante = file.getAbsolutePath();

            executeAsync(() -> {
                imageOriginale = OutilsImage.convertionCheminEnBufferedImage(cheminImageCourante);
                return imageOriginale;
            }, img -> {
                ((JTextField)components.get("fieldPath")).setText(file.getName());
                afficherImage(img, (JLabel)components.get("imageOriginale"));

                // Préparer comparaison
                JPanel comp = (JPanel)components.get("comparison");
                comp.removeAll();
                comp.add(createTitledPanel("Original", createImageWithDisplay(img)));
                comp.add(createTitledPanel("Filtré", createImageLabel("", 600, 450)));
                comp.revalidate();

                tabbedPane.setEnabledAt(1, true);
                ((JButton)components.get("btnApplyFilter")).setEnabled(true);
            }, "Chargement image");
        }
    }

    private void appliquerFiltre(Object e) {
        executeAsync(() -> {
            JComboBox combo = (JComboBox)components.get("typedefiltre");
            JSpinner size = (JSpinner)components.get("taille");
            JSpinner sigma = (JSpinner)components.get("sigma");

            Filtre filtre = combo.getSelectedIndex() == 0 ?
                    new FiltreFlouGaussien((Integer)size.getValue(), (Double)sigma.getValue()) :
                    new FiltreFlouMoyenne((Integer)size.getValue());

            String temp = "./temp/filtered.jpg";
            Files.createDirectories(Paths.get("./temp"));
            filtre.appliquerFiltre(cheminImageCourante, temp);

            return OutilsImage.convertionCheminEnBufferedImage(temp);
        }, img -> {
            imageFiltree = img;
            JPanel comp = (JPanel)components.get("comparison");
            // Récupérer le deuxième panneau (image filtrée)
            Component secondPanel = comp.getComponent(1);
            JLabel filtered;
            if (secondPanel instanceof JPanel) {
                JPanel panel = (JPanel)secondPanel;
                // Le JLabel est dans le JScrollPane qui est dans le JPanel
                Component child = panel.getComponent(0);
                if (child instanceof JScrollPane) {
                    filtered = (JLabel)((JScrollPane)child).getViewport().getView();
                } else if (child instanceof JLabel) {
                    filtered = (JLabel)child;
                } else {
                    filtered = createImageLabel("", 600, 450);
                    panel.add(new JScrollPane(filtered));
                }
            } else {
                filtered = createImageLabel("", 600, 450);
            }
            afficherImage(img, filtered);

            tabbedPane.setEnabledAt(2, true);
            ((JButton)components.get("btnDetectBiomes")).setEnabled(true);
        }, "Application filtre");
    }

    private void detecterBiomes(int algoIndex, int nbBiomes, double eps, int minPts, int metricIndex) {
        executeAsync(() -> {
            // Créer l'algorithme selon le choix
            AlgorithmeClustering algorithm;
            if (algoIndex == 0) { // K-Means
                algorithm = Algorithmes.kmeans(nbBiomes);
            } else if (algoIndex == 1) { // DBSCAN standard
                algorithm = Algorithmes.dbscan(eps, minPts);
            } else { // DBSCAN Optimisé
                algorithm = Algorithmes.dbscanOpti(eps, minPts);
            }

            // Choisir la métrique
            TypeClustering type;
            switch (metricIndex) {
                case 0: type = TypeClustering.BIOMES_CIELAB; break;
                case 1: type = TypeClustering.BIOMES_CIE94; break;
                case 2: type = TypeClustering.BIOMES_EUCLIDIENNE; break;
                default: type = TypeClustering.BIOMES_REDMEAN; break;
            }

            return manager.clusteriserImage(imageFiltree, algorithm, type);
        }, result -> {
            resultatBiomes = result;
            etiquettesBiomes = visuBiomes.etiquerBiomes(result);

            // Afficher résultats
            afficherResultatsBiomes();

            // Remplir combo biomes dans panel biomes
            JComboBox combo = (JComboBox)components.get("comboBiome");
            combo.removeAllItems();
            for (int i = 0; i < result.nombreClusters; i++) {
                combo.addItem("Biome " + i + ": " + etiquettesBiomes[i]);
            }
            combo.setEnabled(true);
            ((JButton)components.get("btnShowBiome")).setEnabled(true);

            // Remplir aussi le combo dans panel écosystèmes
            JComboBox comboBiomeEco = (JComboBox)components.get("comboBiomeEco");
            comboBiomeEco.removeAllItems();
            for (int i = 0; i < result.nombreClusters; i++) {
                comboBiomeEco.addItem("Biome " + i + ": " + etiquettesBiomes[i]);
            }
            comboBiomeEco.setEnabled(true);

            tabbedPane.setEnabledAt(3, true);
            components.get("btnDetectEco").setEnabled(true);
            components.get("btnDetectAllEco").setEnabled(true);
        }, "Détection biomes");
    }

    private void afficherBiome(Object e) {
        JComboBox combo = (JComboBox)components.get("comboBiome");
        if (combo.getSelectedItem() == null) return;

        String selection = (String)combo.getSelectedItem();
        biomeSelectionne = Integer.parseInt(selection.split(":")[0].replace("Biome ", ""));

        BufferedImage biomeImg = visuBiomes.creerImageBiomeIsole(imageFiltree, resultatBiomes, biomeSelectionne);

        JLabel biomeLabel = (JLabel)components.get("biomesSelectedLabel");
        afficherImage(biomeImg, biomeLabel);

        components.get("btnDetectEco").setEnabled(true);
    }

    private void detecterEcosystemes(boolean tous) {
        executeAsync(() -> {
            JComboBox algo = (JComboBox)components.get("algoEco");
            int algoIndex = algo.getSelectedIndex();
            int k = (Integer)((JSpinner)components.get("spinnerK")).getValue();
            double eps = (Double)((JSpinner)components.get("spinnerEps")).getValue();
            int minPts = (Integer)((JSpinner)components.get("spinnerMinPts")).getValue();

            if (tous) {
                // Analyser TOUS les biomes
                resultatsEcosystemes.clear();
                for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
                    resultatsEcosystemes.add(null);
                }

                for (int i = 0; i < resultatBiomes.nombreClusters; i++) {
                    PixelData[] pixels = resultatBiomes.getPixelsCluster(i);
                    if (pixels.length < 50) continue;

                    AlgorithmeClustering algorithm = createEcoAlgorithm(algoIndex, k, eps, minPts);
                    ResultatClustering eco = manager.clusteriserSousEnsemble(
                            pixels, algorithm, TypeClustering.ECOSYSTEMES_POSITION);
                    resultatsEcosystemes.set(i, eco);
                }
                return null; // Indique qu'on a analysé tous les biomes
            } else if (biomeSelectionne >= 0) {
                // Analyser UN SEUL biome
                PixelData[] pixels = resultatBiomes.getPixelsCluster(biomeSelectionne);
                if (pixels.length < 50) {
                    throw new RuntimeException("Biome trop petit pour l'analyse");
                }

                AlgorithmeClustering algorithm = createEcoAlgorithm(algoIndex, k, eps, minPts);
                ResultatClustering eco = manager.clusteriserSousEnsemble(
                        pixels, algorithm, TypeClustering.ECOSYSTEMES_POSITION);

                while (resultatsEcosystemes.size() <= biomeSelectionne) {
                    resultatsEcosystemes.add(null);
                }
                resultatsEcosystemes.set(biomeSelectionne, eco);

                return eco;
            }
            return null;
        }, result -> {
            // Remplir le combo de visualisation
            JComboBox comboBiomeVisu = (JComboBox)components.get("comboBiomeVisu");
            comboBiomeVisu.removeAllItems();

            for (int i = 0; i < resultatsEcosystemes.size(); i++) {
                if (resultatsEcosystemes.get(i) != null && resultatsEcosystemes.get(i).nombreClusters > 0) {
                    comboBiomeVisu.addItem("Biome " + i + ": " + etiquettesBiomes[i] +
                            " (" + resultatsEcosystemes.get(i).nombreClusters + " écosystèmes)");
                }
            }

            comboBiomeVisu.setEnabled(comboBiomeVisu.getItemCount() > 0);
            ((JButton)components.get("btnShowBiomeEco")).setEnabled(comboBiomeVisu.getItemCount() > 0);

            // Si on a analysé un seul biome, l'afficher directement
            if (result != null && biomeSelectionne >= 0) {
                afficherEcosystemesBiome(biomeSelectionne, result);
                // Sélectionner ce biome dans le combo
                for (int i = 0; i < comboBiomeVisu.getItemCount(); i++) {
                    String item = (String)comboBiomeVisu.getItemAt(i);
                    if (item.startsWith("Biome " + biomeSelectionne + ":")) {
                        comboBiomeVisu.setSelectedIndex(i);
                        break;
                    }
                }
            }

            tabbedPane.setEnabledAt(4, true);
            afficherStatsEcosystemes();
        }, "Détection écosystèmes");
    }

    private void afficherBiomeAvecEcosystemes() {
        JComboBox combo = (JComboBox)components.get("comboBiomeVisu");
        if (combo.getSelectedItem() == null) return;

        String selection = (String)combo.getSelectedItem();
        int biomeIndex = Integer.parseInt(selection.split(":")[0].replace("Biome ", ""));

        ResultatClustering ecosystemes = resultatsEcosystemes.get(biomeIndex);
        if (ecosystemes != null) {
            afficherEcosystemesBiome(biomeIndex, ecosystemes);
            afficherStatsBiomeEcosystemes(biomeIndex, ecosystemes);
        }
    }

    private void afficherStatsBiomeEcosystemes(int biomeIndex, ResultatClustering ecosystemes) {
        JPanel stats = (JPanel)components.get("ecoStats");
        stats.removeAll();

        // Stats globales du biome
        PixelData[] pixelsBiome = resultatBiomes.getPixelsCluster(biomeIndex);
        stats.add(createStatsPanel("Biome " + biomeIndex + ": " + etiquettesBiomes[biomeIndex],
                "Pixels du biome: " + pixelsBiome.length,
                "Écosystèmes détectés: " + ecosystemes.nombreClusters,
                "Algorithme: " + ecosystemes.algorithme,
                "Temps: " + ecosystemes.dureeMs + " ms"
        ));

        // Détail de chaque écosystème
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Détail des écosystèmes"));

        for (int i = 0; i < ecosystemes.nombreClusters; i++) {
            int nbPixels = 0;
            int sumX = 0, sumY = 0;

            for (int j = 0; j < ecosystemes.affectations.length; j++) {
                if (ecosystemes.affectations[j] == i) {
                    nbPixels++;
                    sumX += pixelsBiome[j].getX();
                    sumY += pixelsBiome[j].getY();
                }
            }

            if (nbPixels > 0) {
                double pourcentage = (nbPixels * 100.0) / pixelsBiome.length;
                JPanel ecoPanel = new JPanel(new GridLayout(2, 1));
                ecoPanel.setBorder(BorderFactory.createEtchedBorder());
                ecoPanel.add(new JLabel("Écosystème " + i));
                ecoPanel.add(new JLabel(String.format("%d pixels (%.1f%%), centre: (%d, %d)",
                        nbPixels, pourcentage, sumX/nbPixels, sumY/nbPixels)));
                detailPanel.add(ecoPanel);
            }
        }

        stats.add(new JScrollPane(detailPanel));
        stats.revalidate();
        stats.repaint();
    }

    private AlgorithmeClustering createEcoAlgorithm(int index, int k, double eps, int minPts) {
        switch (index) {
            case 0: return Algorithmes.dbscanOpti(eps, minPts);
            case 1: return Algorithmes.kmeans(k);
            default: return Algorithmes.dbscan(eps, minPts);
        }
    }

    private void exporter(String type) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            executeAsync(() -> {
                String nom = new File(cheminImageCourante).getName().replaceAll("\\.[^.]+$", "");
                String dir = chooser.getSelectedFile().getAbsolutePath() + "/" + nom;

                if (!type.equals("ecosystemes") && resultatBiomes != null) {
                    visuBiomes.sauvegarderTousBiomes(imageFiltree, resultatBiomes, dir, nom);
                }

                if (!type.equals("biomes") && !resultatsEcosystemes.isEmpty()) {
                    String ecoDir = dir + "/ecosystemes";
                    Files.createDirectories(Paths.get(ecoDir));

                    for (int i = 0; i < resultatsEcosystemes.size(); i++) {
                        if (resultatsEcosystemes.get(i) != null) {
                            PixelData[] pixels = resultatBiomes.getPixelsCluster(i);
                            visuEcosystemes.sauvegarderEcosystemesBiome(
                                    imageFiltree, pixels, resultatsEcosystemes.get(i),
                                    ecoDir, etiquettesBiomes[i], i);
                        }
                    }
                }
                return dir;
            }, dir -> {
                JOptionPane.showMessageDialog(this, "Export terminé dans: " + dir);
            }, "Export");
        }
    }

    // === MÉTHODES D'AFFICHAGE ===

    private void afficherResultatsBiomes() {
        BufferedImage img = visuBiomes.creerImageBiomes(imageFiltree, resultatBiomes);
        JLabel global = (JLabel)components.get("biomesGlobalLabel");
        afficherImage(img, global);

        // Stats
        JPanel stats = (JPanel)components.get("biomesStats");
        stats.removeAll();

        stats.add(createStatsPanel("Résumé",
                "Algorithme: " + resultatBiomes.algorithme,
                "Biomes: " + resultatBiomes.nombreClusters,
                "Temps: " + resultatBiomes.dureeMs + " ms"
        ));

        stats.revalidate();
    }

    private void afficherEcosystemesBiome(int biome, ResultatClustering eco) {
        PixelData[] pixels = resultatBiomes.getPixelsCluster(biome);
        BufferedImage fond = visuBiomes.creerFondClair(imageFiltree);
        BufferedImage img = visuEcosystemes.creerImageEcosystemesSurFondClair(fond, pixels, eco);

        JLabel label = (JLabel)components.get("ecoLabel");
        afficherImage(img, label);

        // Mettre à jour la sélection du biome dans le combo
        JComboBox comboBiomeEco = (JComboBox)components.get("comboBiomeEco");
        if (comboBiomeEco.getItemCount() > biome) {
            comboBiomeEco.setSelectedIndex(biome);
        }
    }

    private void afficherStatsEcosystemes() {
        JPanel stats = (JPanel)components.get("ecoStats");
        stats.removeAll();

        int total = 0, analyses = 0;
        for (ResultatClustering eco : resultatsEcosystemes) {
            if (eco != null) {
                total += eco.nombreClusters;
                analyses++;
            }
        }

        stats.add(createStatsPanel("Résumé global",
                "Total écosystèmes: " + total,
                "Biomes analysés: " + analyses + "/" + resultatBiomes.nombreClusters
        ));

        // Liste des biomes analysés
        JPanel listeBiomes = new JPanel();
        listeBiomes.setLayout(new BoxLayout(listeBiomes, BoxLayout.Y_AXIS));
        listeBiomes.setBorder(BorderFactory.createTitledBorder("Biomes analysés"));

        for (int i = 0; i < resultatsEcosystemes.size(); i++) {
            if (resultatsEcosystemes.get(i) != null) {
                ResultatClustering eco = resultatsEcosystemes.get(i);
                JLabel label = new JLabel(String.format("Biome %d (%s): %d écosystèmes",
                        i, etiquettesBiomes[i], eco.nombreClusters));
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                listeBiomes.add(label);
            }
        }

        stats.add(listeBiomes);
        stats.revalidate();
    }

    // === UTILITAIRES UI ===

    private JPanel createConfigPanel(String title, Object[][] config) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < config.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            panel.add(new JLabel((String)config[i][0]), gbc);
            gbc.gridx = 1;
            JComponent comp = (JComponent)config[i][1];
            panel.add(comp, gbc);

            // Stocker les références avec des clés basées sur le label
            String key = ((String)config[i][0]).toLowerCase()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("(", "")
                    .replace(")", "");
            components.put(key, comp);
        }

        return panel;
    }

    private JPanel createStatsPanel(String title, String... stats) {
        JPanel panel = new JPanel(new GridLayout(stats.length, 1));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        for (String stat : stats) {
            panel.add(new JLabel(stat));
        }
        return panel;
    }

    private JPanel createFlowPanel(Component... comps) {
        JPanel panel = new JPanel(new FlowLayout());
        for (Component c : comps) panel.add(c);
        return panel;
    }

    private JPanel createTitledPanel(String title, Component comp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(comp instanceof JScrollPane ? comp : new JScrollPane(comp));
        return panel;
    }

    private JLabel createLabel(String text, int size, int style) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", style, size));
        return label;
    }

    private JLabel createImageLabel(String text, int w, int h) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setPreferredSize(new Dimension(w, h));
        return label;
    }

    private JLabel createImageWithDisplay(BufferedImage img) {
        JLabel label = createImageLabel("", 600, 450);
        afficherImage(img, label);
        return label;
    }

    private JButton createButton(String text, Consumer<Object> action) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> action.accept(e));
        return btn;
    }

    private JTextField createTextField(int cols, boolean editable) {
        JTextField field = new JTextField(cols);
        field.setEditable(editable);
        return field;
    }

    private JTextArea createTextArea(String text, boolean editable) {
        JTextArea area = new JTextArea(text);
        area.setEditable(editable);
        area.setFont(new Font("Arial", Font.PLAIN, 13));
        return area;
    }

    private JComboBox<String> createCombo(String... items) {
        JComboBox<String> combo = new JComboBox<>(items);
        // Ne pas stocker automatiquement si pas d'items (combo vide)
        if (items.length > 0) {
            components.put("combo" + items[0].replace(" ", ""), combo);
        }
        return combo;
    }

    private JSpinner createSpinner(Number value, Number min, Number max, Number step) {
        SpinnerModel model = value instanceof Integer ?
                new SpinnerNumberModel((Integer)value, (Integer)min, (Integer)max, (Integer)step) :
                new SpinnerNumberModel((Double)value, (Double)min, (Double)max, (Double)step);
        JSpinner spinner = new JSpinner(model);
        components.put("spinner" + value, spinner);
        return spinner;
    }

    private JPanel createProgressPanel(JProgressBar bar) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Progression"));
        panel.add(bar);
        panel.setPreferredSize(new Dimension(0, 80));
        return panel;
    }

    private void afficherImage(BufferedImage img, JLabel label) {
        if (img == null) return;

        int w = img.getWidth(), h = img.getHeight();
        Dimension size = label.getPreferredSize();
        double scale = Math.min((double)size.width/w, (double)size.height/h);

        if (scale < 1) {
            w = (int)(w * scale);
            h = (int)(h * scale);
            img = toBufferedImage(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        }

        label.setIcon(new ImageIcon(img));
        label.setText("");
    }

    private BufferedImage toBufferedImage(Image img) {
        BufferedImage bimg = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bimg;
    }

    // === EXÉCUTION ASYNCHRONE ===

    private <T> void executeAsync(ThrowingSupplier<T> task, Consumer<T> onSuccess, String taskName) {
        JProgressBar bar = progressBars.get(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
        if (bar != null) {
            bar.setIndeterminate(true);
            bar.setString(taskName + "...");
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                onSuccess.accept(result);
                if (bar != null) {
                    bar.setIndeterminate(false);
                    bar.setString(taskName + " terminé");
                }
            });
        }).exceptionally(e -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                if (bar != null) {
                    bar.setIndeterminate(false);
                    bar.setString("Erreur");
                }
            });
            return null;
        });
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Utiliser le look par défaut
        }

        SwingUtilities.invokeLater(() -> {
            new MainInterface().setVisible(true);
        });
    }
}