package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.model.CouplingEdge;
import tp1.partie1.ex3.model.Dendrogram;
import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.*;
import tp1.partie1.ex3.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
public class AnalyzerUI extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();

    // Images
    private final ImagePanel callGraphPanel      = new ImagePanel();
    private final ImagePanel couplingGraphPanel  = new ImagePanel();
    private final ImagePanel dendrogramPanel     = new ImagePanel();
    private final ImagePanel modulesGraphPanel   = new ImagePanel();

    // Tables
    private final JTable couplingTable = new JTable(new DefaultTableModel(
            new Object[]{"classA", "classB", "count", "weight"}, 0));
    private final JTable modulesTable = new JTable(new DefaultTableModel(
            new Object[]{"module_id", "avgSimilarity", "classes"}, 0));

    public AnalyzerUI() {
        super("TP – Call Graph, Coupling & Modules");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 860);
        setLocationRelativeTo(null);

        // Toolbar
        JButton chooseBtn = new JButton("Choisir dossier src…");
        JButton runBtn = new JButton("Analyser");
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("–");
        JButton zoomReset = new JButton("100%");

        JComboBox<String> targetCombo = new JComboBox<>(new String[]{
                "Call Graph", "Coupling", "Dendrogram", "Modules"
        });

        // seuil CP
        JLabel cpLbl = new JLabel("CP:");
        JSpinner cpSpinner = new JSpinner(new SpinnerNumberModel(0.10, 0.0, 1.0, 0.01));
        ((JSpinner.NumberEditor)cpSpinner.getEditor()).getFormat().setMinimumFractionDigits(2);

        JToolBar tb = new JToolBar(); tb.setFloatable(false);
        tb.add(chooseBtn); tb.add(runBtn);
        tb.addSeparator();
        tb.add(new JLabel("Zoom: "));
        tb.add(zoomOut); tb.add(zoomReset); tb.add(zoomIn);
        tb.addSeparator();
        tb.add(new JLabel("Cible: ")); tb.add(targetCombo);
        tb.addSeparator();
        tb.add(cpLbl); tb.add(cpSpinner);

        // Onglets
        tabs.add("Call Graph (PNG)",     wrap(callGraphPanel));
        tabs.add("Coupling (PNG)",       wrap(couplingGraphPanel));
        tabs.add("Coupling (Table)",     new JScrollPane(couplingTable));
        tabs.add("Dendrogram (PNG)",     wrap(dendrogramPanel));
        tabs.add("Modules (PNG)",        wrap(modulesGraphPanel));
        tabs.add("Modules (Table)",      new JScrollPane(modulesTable));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);

        // Actions
        final File[] selectedSrc = {null};
        chooseBtn.addActionListener(ae -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Dossier src/ à analyser");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedSrc[0] = fc.getSelectedFile();
                setTitle("TP – " + selectedSrc[0].getAbsolutePath());
            }
        });

        runBtn.addActionListener(ae -> runAnalysis(selectedSrc[0], ((Number)cpSpinner.getValue()).doubleValue()));

        zoomIn.addActionListener(ae -> getTargetPanel(targetCombo).scale(1.10));
        zoomOut.addActionListener(ae -> getTargetPanel(targetCombo).scale(1/1.10));
        zoomReset.addActionListener(ae -> getTargetPanel(targetCombo).reset());

        // synchroniser “Cible” avec l'onglet actif (ergonomie)
        tabs.addChangeListener(e -> targetCombo.setSelectedIndex(tabs.getSelectedIndex() < 3 ? tabs.getSelectedIndex() : tabs.getSelectedIndex()));

        setVisible(true);
    }

    private ImagePanel getTargetPanel(JComboBox<String> targetCombo) {
        switch (String.valueOf(targetCombo.getSelectedItem())) {
            case "Call Graph":  return callGraphPanel;
            case "Coupling":    return couplingGraphPanel;
            case "Dendrogram":  return dendrogramPanel;
            default:            return modulesGraphPanel;
        }
    }

    private JScrollPane wrap(ImagePanel p) {
        var sp = new JScrollPane(p);
        p.setBackground(Color.WHITE);
        p.addMouseWheelListener(new MouseAdapter() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) p.scale(e.getWheelRotation() < 0 ? 1.10 : 1/1.10);
            }
        });
        return sp;
    }

    private void runAnalysis(File srcDir, double CP) {
        if (srcDir == null) {
            JOptionPane.showMessageDialog(this, "Veuillez choisir un dossier src/ d’abord.");
            return;
        }
        try {
            var reporter = new ConsoleReporter();
            var scanner = new SourceScanner();
            var cuFactory = new CompilationUnitFactory(srcDir.getAbsolutePath());
            List<File> files = scanner.listJavaFiles(srcDir);
            if (files.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun .java trouvé.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 1) Analyse d’appels
            var analysis = new AnalysisService(cuFactory, reporter);
            analysis.analyze(files);

            // Call graph PNG
            runDot("target/callgraph.dot", "target/callgraph.png");

            // 2) Couplage
            var coupling = new CouplingAnalyzer().compute(analysis.getCallEdges(), analysis.getProjectTypes());

            // CSV coupling
            StringBuilder csv = new StringBuilder("classA,classB,count,weight\n");
            for (CouplingEdge e : coupling) {
                csv.append(e.classA()).append(",").append(e.classB()).append(",")
                   .append(e.count()).append(",")
                   .append(String.format(Locale.US, "%.6f", e.weight())).append("\n");
            }
            Files.createDirectories(new File("target").toPath());
            Files.writeString(new File("target/coupling.csv").toPath(), csv.toString(), StandardCharsets.UTF_8);

            // Coupling DOT -> PNG
            new CouplingDotExporter().exportUndirected(coupling, "target/coupling.dot");
            runDot("target/coupling.dot", "target/coupling.png");

            // 3) Clustering & Modules
            var matrix = new CouplingMatrix(analysis.getProjectTypes(), coupling);
            Dendrogram dendro = new HierarchicalClustering().fit(matrix);

            new DendrogramDotExporter().export(dendro, "target/dendrogram.dot");
            runDot("target/dendrogram.dot", "target/dendrogram.png");

            var modules = new ModuleExtractor().extract(dendro, matrix, CP);
            new ModulesCsvExporter().export(modules, "target/modules.csv");
            new ModulesDotExporter().export(modules, "target/modules.dot");
            runDot("target/modules.dot", "target/modules.png");

            // 4) Affichage
            callGraphPanel.setImage(new File("target/callgraph.png"));
            couplingGraphPanel.setImage(new File("target/coupling.png"));
            dendrogramPanel.setImage(new File("target/dendrogram.png"));
            modulesGraphPanel.setImage(new File("target/modules.png"));

            loadCsvIntoTable(new File("target/coupling.csv"), couplingTable);
            loadCsvIntoTable(new File("target/modules.csv"), modulesTable);

            JOptionPane.showMessageDialog(this, "Analyse terminée.\nPNG + CSV dans target/\nCP = " + String.format(Locale.US,"%.2f", CP));

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void runDot(String dot, String png) throws Exception {
        if (!new File(dot).exists()) return;
        try {
            new ProcessBuilder("dot", "-Tpng", dot, "-o", png).inheritIO().start().waitFor();
        } catch (Exception e) {
            // Graphviz absent -> pas de PNG
        }
    }

    private void loadCsvIntoTable(File csv, JTable table) throws IOException {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), StandardCharsets.UTF_8))) {
            String header = br.readLine(); // skip header
            for (String line; (line = br.readLine()) != null; ) {
                if (line.isBlank()) continue;
                // gère les classes entre guillemets dans modules.csv
                String[] parts = parseCsvLine(line);
                m.addRow(parts);
            }
        }
    }

    private static String[] parseCsvLine(String line) {
        // très simple: split sur virgules sauf si entre guillemets
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        for (char c: line.toCharArray()) {
            if (c=='"') { quoted = !quoted; continue; }
            if (c==',' && !quoted) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // --- Panel image zoomable ---
    static class ImagePanel extends JPanel {
        private Image img;
        private double scale = 1.0;

        public void setImage(File file) {
            if (file.exists()) {
                this.img = new ImageIcon(file.getAbsolutePath()).getImage();
                this.scale = 1.0;
                revalidate(); repaint();
            }
        }
        public void scale(double factor) { this.scale *= factor; repaint(); }
        public void reset() { this.scale = 1.0; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            int w = (int)(img.getWidth(null) * scale);
            int h = (int)(img.getHeight(null) * scale);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, w, h, null);
            setPreferredSize(new Dimension(w, h));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AnalyzerUI::new);
    }
}
