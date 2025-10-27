package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.ConsoleReporter;
import tp1.partie1.ex3.report.CouplingDotExporter;
import tp1.partie1.ex3.service.AnalysisService;
import tp1.partie1.ex3.service.CouplingAnalyzer;
import tp1.partie1.ex3.model.CouplingEdge;

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
    private final ImagePanel callGraphPanel = new ImagePanel();
    private final ImagePanel couplingGraphPanel = new ImagePanel();
    private final JTable couplingTable = new JTable(new DefaultTableModel(
            new Object[]{"classA", "classB", "count", "weight"}, 0));

    public AnalyzerUI() {
        super("TP – Graphe d’appel et Couplage");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Toolbar
        JButton chooseBtn = new JButton("Choisir dossier src…");
        JButton runBtn = new JButton("Analyser");
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("–");
        JButton zoomReset = new JButton("100%");
        JComboBox<String> targetCombo = new JComboBox<>(new String[]{"Call Graph", "Coupling"});

        JToolBar tb = new JToolBar(); tb.setFloatable(false);
        tb.add(chooseBtn); tb.add(runBtn);
        tb.addSeparator(); tb.add(new JLabel("Zoom: "));
        tb.add(zoomOut); tb.add(zoomReset); tb.add(zoomIn);
        tb.addSeparator(); tb.add(new JLabel("Cible: ")); tb.add(targetCombo);

        // Tabs
        tabs.add("Call Graph (PNG)", wrap(callGraphPanel));
        tabs.add("Coupling (PNG)", wrap(couplingGraphPanel));
        tabs.add("Coupling (Table)", new JScrollPane(couplingTable));

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

        runBtn.addActionListener(ae -> runAnalysis(selectedSrc[0]));

        zoomIn.addActionListener(ae -> getTargetPanel(targetCombo).scale(1.10));
        zoomOut.addActionListener(ae -> getTargetPanel(targetCombo).scale(1/1.10));
        zoomReset.addActionListener(ae -> getTargetPanel(targetCombo).reset());

        setVisible(true);
    }

    private ImagePanel getTargetPanel(JComboBox<String> targetCombo) {
        return targetCombo.getSelectedItem().toString().startsWith("Call") ? callGraphPanel : couplingGraphPanel;
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

    private void runAnalysis(File srcDir) {
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

            // 2) Couplage
            var coupling = new CouplingAnalyzer().compute(analysis.getCallEdges(), analysis.getProjectTypes());
            // CSV
            StringBuilder csv = new StringBuilder("classA,classB,count,weight\n");
            for (CouplingEdge e : coupling) {
                csv.append(e.classA()).append(",").append(e.classB()).append(",")
                   .append(e.count()).append(",")
                   .append(String.format(Locale.US, "%.6f", e.weight())).append("\n");
            }
            Files.createDirectories(new File("target").toPath());
            Files.writeString(new File("target/coupling.csv").toPath(), csv.toString(), StandardCharsets.UTF_8);

            // 3) Exports DOT -> PNG
            new CouplingDotExporter().exportUndirected(coupling, "target/coupling.dot");
            runDot("target/coupling.dot", "target/coupling.png");

            runDot("target/callgraph.dot", "target/callgraph.png"); // callgraph généré par AnalysisService

            // 4) Affichage
            callGraphPanel.setImage(new File("target/callgraph.png"));
            couplingGraphPanel.setImage(new File("target/coupling.png"));
            loadCsvIntoTable(new File("target/coupling.csv"));

            JOptionPane.showMessageDialog(this, "Analyse terminée.\nPNG + CSV dans target/");

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
            // Graphviz pas installé: on laisse le PNG vide pour l’onglet
        }
    }

    private void loadCsvIntoTable(File csv) throws IOException {
        DefaultTableModel m = (DefaultTableModel) couplingTable.getModel();
        m.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv), StandardCharsets.UTF_8))) {
            String header = br.readLine(); // skip header
            for (String line; (line = br.readLine()) != null; ) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                m.addRow(parts);
            }
        }
    }

    // --- Panel image zoomable ---
    @SuppressWarnings("serial")
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
