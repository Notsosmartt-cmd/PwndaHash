package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Gameplay extends JPanel implements KeyListener, ActionListener {
    private boolean play = true;
    private int score = 0;
    private Timer gameTimer;
    private final int delay = 1;

    private int rows = 5;
    private int pegRadius = 10;
    private int offsetX = 350;
    private int offsetY = 100;
    private int spacing = 100;

    int gameWidth = 700;
    int gameHeight = 600;

    public static String password; // Updated for dynamic input null if none
    public String zipFilePath; // Path to the ZIP file
    private String inputfile = "plans.txt";
    
    
    // Game components
    private BucketGenerator bucketGenerator;
    private BallManager ballManager;
    private CollisionDetector collisionDetector;
    private GameRenderer renderer;
    private PegGenerator pegGenerator;

    public Gameplay(String password, String zipFilePath) {
        this.password = password;
        this.zipFilePath = zipFilePath;

        // Load password dynamically
        if (password == null || password.isEmpty()) {
            this.password = loadPasswordFromZip(zipFilePath);
        }

        if (this.password == null) {
            throw new IllegalArgumentException("A valid password must be provided!");
        }

        // Initialize game components
        bucketGenerator = new BucketGenerator(this.password, gameWidth, gameHeight);
        ballManager = new BallManager(inputfile, 700);
        pegGenerator = new PegGenerator(rows, pegRadius, offsetX, offsetY, spacing);
        collisionDetector = new CollisionDetector(bucketGenerator, ballManager, score, this.password, pegGenerator.getPegs());
        renderer = new GameRenderer(bucketGenerator, ballManager, collisionDetector, pegGenerator);

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        gameTimer = new Timer(delay, this);
        gameTimer.start();

        setLayout(null);
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        renderer.paintComponent(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (play) {
            collisionDetector.detectCollisions();
            score = collisionDetector.getScore();
            renderer.updateScore(score);

            if (!ballManager.hasPendingBalls() && ballManager.getActiveBalls().isEmpty()) {
                play = false;
                renderer.setPlay(false);
                collisionDetector.sortAndTransform();
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!play) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                restartGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private void restartGame() {
        play = true;
        score = 0;
        collisionDetector = new CollisionDetector(bucketGenerator, ballManager, score, password, pegGenerator.getPegs());
        renderer.setPlay(true);
        renderer.updateScore(score);
        gameTimer.start();
        repaint();
    }

    private String loadPasswordFromZip(String zipFilePath) {
        if (zipFilePath == null || zipFilePath.isEmpty()) return null;

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            ZipEntry entry = zipFile.entries().nextElement(); // Assume one file
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                StringBuilder passwordBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    passwordBuilder.append(line);
                }
                return passwordBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
