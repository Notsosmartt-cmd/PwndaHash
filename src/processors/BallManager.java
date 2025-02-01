package processors;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Timer;

public class BallManager {
    private final List<Ball> activeBalls;
    private final List<Ball> pendingBalls;
    private Timer spawnTimer;
    private final int spawnDelay = 1; // Spawn delay in milliseconds
    private final String filename;
    private final int windowWidth;
    private final float gravity = 0.7f; // Adjust this value as needed
    private final ExecutorService executorService;

    public BallManager(String filename, int windowWidth) {
        this.activeBalls = new ArrayList<>();
        this.pendingBalls = new ArrayList<>();
        this.filename = filename;
        this.windowWidth = windowWidth;
        this.executorService = Executors.newSingleThreadExecutor();
        startFileReading();
        startSpawnTimer();
    }



    private void startFileReading() {
        executorService.execute(() -> {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
                Random random = new Random();
                int radius = 10;
                int y = 10; // Starting y-position for all balls
                int i = 0; // Ball index
                int codePoint;

                while ((codePoint = reader.read()) != -1) { // Read one code point at a time
                    int x = random.nextBoolean() ? 300 : 400; // Randomly assign 300 or 400
                    float speedY = random.nextFloat() * 10f + 3f;
                    float speedX = random.nextBoolean() ? -1 : 1;
                    Color color = switch (i % 3) {
                        case 0 -> Color.RED;
                        case 1 -> Color.GREEN;
                        case 2 -> Color.YELLOW;
                        default -> Color.WHITE;
                    };

                    String entry = i + "/" + codePoint;

                    Ball ball = new Ball(x, y, speedX, speedY, radius, color, 1, entry);

                    synchronized (pendingBalls) {
                        pendingBalls.add(ball);
                        
                        
                        
                    }

                    System.out.println("Ball added: " + entry);

                    // Increment ball index
                    i++;

                    Thread.sleep(6); // Simulate delay in reading/processing
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error reading file: " + e.getMessage());
            }
        });
    }

    private void startSpawnTimer() {
        spawnTimer = new Timer(spawnDelay, e -> spawnBall());
        spawnTimer.start();
    }

    private void spawnBall() {
        synchronized (pendingBalls) {
            if (!pendingBalls.isEmpty()) {
                activeBalls.add(pendingBalls.remove(0));
            } else if (executorService.isShutdown()) {
                spawnTimer.stop();
            }
        }
    }

    public List<Ball> getActiveBalls() {
        return activeBalls;
    }

    public void removeBall(Ball ball) {
        activeBalls.remove(ball);
    }

    public boolean hasPendingBalls() {
        synchronized (pendingBalls) {
            return !pendingBalls.isEmpty();
        }
    }

    public void reset(String filename) {
        executorService.shutdownNow();
        activeBalls.clear();
        synchronized (pendingBalls) {
            pendingBalls.clear();
        }
        startFileReading();
        startSpawnTimer();
    }

    public float getGravity() {
        return gravity;
    }
}
