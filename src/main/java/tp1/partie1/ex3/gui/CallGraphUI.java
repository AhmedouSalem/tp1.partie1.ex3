package tp1.partie1.ex3.gui;

import tp1.partie1.ex3.parser.CompilationUnitFactory;
import tp1.partie1.ex3.parser.SourceScanner;
import tp1.partie1.ex3.report.ConsoleReporter;
import tp1.partie1.ex3.service.AnalysisService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.List;

@SuppressWarnings("serial")
public class CallGraphUI extends JFrame {
    private final JLabel imageLabel = new JLabel();
    private double scale = 1.0;

    public CallGraphUI() {
        super("Graphe d’appel – TP");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Barre de boutons
        JButton openBtn = new JButton("Choisir dossier src…");
        JButton refreshBtn = new JButton("Rafraîchir");
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("–");
        JButton zoomReset = new JButton("100%");

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.add(openBtn);
        tb.add(refreshBtn);
        tb.addSeparator();
        tb.add(new JLabel("Zoom: "));
        tb.add(zoomOut);
        tb.add(zoomReset);
        tb.add(zoomIn);

        // Zone image scrollable
        JScrollPane scroll = new JScrollPane(imageLabel);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.addMouseWheelListener(new MouseAdapter() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) scale *= 1.1;
                    else scale /= 1.1;
                    applyScale();
                }
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        // Actions
        openBtn.addActionListener(ae -> chooseAndRun());
        refreshBtn.addActionListener(ae -> renderDotToPngAndShow(new File("target/callgraph.dot")));

        zoomIn.addActionListener(ae -> { scale *= 1.1; applyScale(); });
        zoomOut.addActionListener(ae -> { scale /= 1.1; applyScale(); });
        zoomReset.addActionListener(ae -> { scale = 1.0; applyScale(); });
    }

    private void chooseAndRun() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choisir le dossier src/ à analyser");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File srcDir = fc.getSelectedFile();
            runAnalysis(srcDir);
        }
    }

    private void runAnalysis(File srcDir) {
        try {
            var reporter = new ConsoleReporter();
            var scanner = new SourceScanner();
            var cuFactory = new CompilationUnitFactory(srcDir.getAbsolutePath());
            List<File> files = scanner.listJavaFiles(srcDir);
            if (files.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun .java dans: " + srcDir, "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            var service = new AnalysisService(cuFactory, reporter);
            service.analyze(files); // écrit target/callgraph.dot

            renderDotToPngAndShow(new File("target/callgraph.dot"));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderDotToPngAndShow(File dotFile) {
        try {
            if (!dotFile.exists()) {
                JOptionPane.showMessageDialog(this, "DOT introuvable: " + dotFile.getAbsolutePath(),
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Vérifier que 'dot' est installé
            if (!hasDot()) {
                JOptionPane.showMessageDialog(this,
                        "Graphviz (commande 'dot') n'est pas installé.\n" +
                        "Installe-le puis réessaie : sudo apt-get install graphviz",
                        "Graphviz requis", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File outDir = new File("target");
            outDir.mkdirs();
            File png = new File(outDir, "callgraph.png");

            // dot -Tpng target/callgraph.dot -o target/callgraph.png
            Process p = new ProcessBuilder("dot", "-Tpng",
                    dotFile.getAbsolutePath(), "-o", png.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();

            if (!png.exists()) {
                JOptionPane.showMessageDialog(this, "Échec rendu PNG.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            ImageIcon icon = new ImageIcon(png.getAbsolutePath());
            imageLabel.setIcon(icon);
            scale = 1.0;
            applyScale();
            setTitle("Graphe d’appel – " + png.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyScale() {
        Icon ic = imageLabel.getIcon();
        if (!(ic instanceof ImageIcon)) return;
        Image img = ((ImageIcon) ic).getImage();
        int w = (int)(img.getWidth(null) * scale);
        int h = (int)(img.getHeight(null) * scale);
        if (w <= 0 || h <= 0) return;
        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
        imageLabel.revalidate();
    }

    private static boolean hasDot() {
        try {
            Process p = new ProcessBuilder("dot", "-V").start();
            p.waitFor();
            return true;
        } catch (Exception e) { return false; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CallGraphUI().setVisible(true));
    }
}
