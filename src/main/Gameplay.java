package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JPanel;
import javax.swing.Timer;

import generators.BucketGenerator;
import generators.PegGenerator;
import proccessors.BallManager;
import proccessors.CollisionDetector;
import proccessors.GameRenderer;


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
    public String inputFilePath; // Changed from zipFilePath to inputFilePath
    public String outputFilePath; // Path to the output file
    
    // Game components
    private BucketGenerator bucketGenerator;
    private BallManager ballManager;
    private CollisionDetector collisionDetector;
    private GameRenderer renderer;
    private PegGenerator pegGenerator;

    public Gameplay(String password, String inputFilePath) {
    	 Gameplay.password = password;
         this.inputFilePath = inputFilePath; // Store the input file path

         // Extract the directory of the input file
         File inputFile = new File(inputFilePath);
         String inputFileName = inputFile.getName();
         String inputDirectory = inputFile.getParent(); // Get the parent directory of the input file
         outputFilePath = inputDirectory + File.separator + ("(Pwnda)" + inputFileName); // Specify output file name and path

         
         
         
         // Load password dynamically (if needed)
         if (password == null || password.isEmpty()) {
             throw new IllegalArgumentException("Password must be provided through GUI!");
         }

     // Initialize game components with selected input file
        ballManager = new BallManager(this.inputFilePath, 700); // Use the provided input file
         
        bucketGenerator = new BucketGenerator(Gameplay.password, gameWidth, gameHeight);
        pegGenerator = new PegGenerator(rows, pegRadius, offsetX, offsetY, spacing);
        collisionDetector = new CollisionDetector(bucketGenerator, ballManager, score, Gameplay.password, pegGenerator.getPegs(), this.inputFilePath);
        renderer = new GameRenderer(bucketGenerator, ballManager, collisionDetector,pegGenerator);
        
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
        collisionDetector = new CollisionDetector(bucketGenerator, ballManager, score, Gameplay.password, pegGenerator.getPegs(), this.inputFilePath);        renderer.setPlay(true);
        renderer.updateScore(score);
        gameTimer.start();
        repaint();
    }

}
