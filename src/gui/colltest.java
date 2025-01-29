package gui;

import java.awt.Rectangle;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class colltest {
    private BucketGenerator bucket;
    private BallManager ballManager;
    private int score;
    private List<String> newest;
    private List<String> transformed;
    private List<PegGenerator.Peg> pegs;

    private final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public colltest(BucketGenerator map, BallManager ballManager, int score, String password,List<PegGenerator.Peg> pegs) {
        this.bucket = map;
        this.ballManager = ballManager;
        this.score = score;
        this.pegs = pegs;
        this.newest = new ArrayList<>();
        this.transformed = new ArrayList<>();
    }

    public int getScore() {
        return score;
    }
    public List<String> getTransformed() {
        return transformed;
    }
    public void startProcessing() {
        Thread collisionThread = new Thread(this::detectCollisionsLoop, "CollisionThread");
        Thread fileWriterThread = new Thread(this::fileWriterLoop, "FileWriterThread");

        collisionThread.start();
        fileWriterThread.start();

        try {
            collisionThread.join();
            running = false; // Signal file writer to stop
            fileWriterThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void detectCollisionsLoop() {
        while (running) {
            detectCollisions();
            try {
                Thread.sleep(16); // Adjust based on desired update rate (e.g., 60 FPS)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void detectCollisions() {
        List<Ball> ballsToRemove = new ArrayList<>();
        Random random = new Random();
        List<String> processedPassword = bucket.processPassword(Gameplay.password);
        List<Ball> activeBalls = ballManager.getActiveBalls();
        
        int[] passwordValArray = new int[processedPassword.size()]; // Create an integer array with the same size

        //int[] passwordValArray = processedPassword.stream()
        //.mapToInt(Integer::parseInt)
       // .toArray();
        
     // Convert each String element to an integer and store in the array
     for (int i = 0; i < processedPassword.size(); i++) {
         passwordValArray[i] = Integer.parseInt(processedPassword.get(i));
     }


        for (int i = 0; i < activeBalls.size(); i++) {
            Ball ball = activeBalls.get(i);

            // Apply gravity and damping
            ball.speedY += ballManager.getGravity();
            ball.speedX *= 0.99; // Horizontal damping
            ball.speedY *= 0.99; // Vertical damping

            // Move ball
            ball.x += ball.speedX;
            ball.y += ball.speedY;

            // Ensure maximum speed
            double maxSpeed = 15.0;
            double speedMagnitude = Math.sqrt(ball.speedX * ball.speedX + ball.speedY * ball.speedY);
            if (speedMagnitude > maxSpeed) {
                ball.speedX *= maxSpeed / speedMagnitude;
                ball.speedY *= maxSpeed / speedMagnitude;
            }

            // Check collision with pegs
            for (PegGenerator.Peg peg : pegs) {
                double dx = ball.x - peg.getCenterX();
                double dy = ball.y - peg.getCenterY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < ball.radius + peg.getRadius()) {
                    double nx = dx / distance;
                    double ny = dy / distance;
                    double dotProduct = ball.speedX * nx + ball.speedY * ny;
                    ball.speedX -= 2 * dotProduct * nx;
                    ball.speedY -= 2 * dotProduct * ny;
                    ball.speedX *= 0.9; // Damping
                    ball.speedY *= 0.9;
                    break;
                }
            }

            // Check collision with bricks (existing logic)...
            // [Existing brick collision code goes here]

         // Boundary collisions
         // Check if the ball goes beyond the left side of the screen
         if (ball.x - ball.radius < 0) {
             ball.x = ball.radius; // Position the ball at the left edge of the screen (x = radius)
             ball.speedX = (float) (-ball.speedX * 0.9); // Reverse the horizontal velocity and apply damping (slows down the ball)
         } 
         // Check if the ball goes beyond the right side of the screen
         else if (ball.x + ball.radius > 700) {
             ball.x = 700 - ball.radius; // Position the ball at the right edge of the screen (x = 700 - radius)
             ball.speedX = (float) (-ball.speedX * 0.9); // Reverse the horizontal velocity and apply damping
         }

         // Check if the ball goes above the top of the screen
         if (ball.y - ball.radius < 0) {
             ball.y = ball.radius; // Position the ball at the top edge of the screen (y = radius)
             ball.speedY = (float) (-ball.speedY * 0.9); // Reverse the vertical velocity and apply damping
         } 
         // Check if the ball goes below the bottom of the screen
         else if (ball.y + ball.radius > 550) {
             ball.y = 550 - ball.radius; // Position the ball at the bottom edge of the screen (y = 550 - radius)

             // Ball hit the floor, delete the ball and calculate a new password entry
             ballsToRemove.add(ball);

             // Process ball's password
             String[] ballParts = ball.getString().split("/");
             int ballFirstNumber = Integer.parseInt(ballParts[0]);
             int ballSecondNumber = Integer.parseInt(ballParts[1]);

             // Randomly select an index from the processed password list
             int passwordIndex = random.nextInt(processedPassword.size());
             int passwordValue = passwordValArray[passwordIndex];

             // Calculate the new sum
             int sum = ballSecondNumber + passwordValue;

             // Create a new password entry with updated values
             String newEntry = ballFirstNumber + "/" + passwordIndex + "/" + sum;
             newest.add(newEntry);

             System.out.println("Ball hit the floor and was removed. New password entry: " + newEntry);
             score += ball.value;
         }

            
            

            // Ball-ball collisions
            for (int j = i + 1; j < activeBalls.size(); j++) {
                Ball other = activeBalls.get(j);

                double dx = ball.x - other.x;
                double dy = ball.y - other.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < ball.radius + other.radius) {
                    // Resolve overlap
                    double overlap = (ball.radius + other.radius) - distance;
                    double nx = dx / distance;
                    double ny = dy / distance;

                    ball.x += nx * overlap / 2;
                    ball.y += ny * overlap / 2;
                    other.x -= nx * overlap / 2;
                    other.y -= ny * overlap / 2;

                    // Calculate new velocities (elastic collision)
                    double vx = ball.speedX - other.speedX;
                    double vy = ball.speedY - other.speedY;
                    double dotProduct = vx * nx + vy * ny;

                    if (dotProduct > 0) continue; // Prevent re-collision

                    double collisionScale = dotProduct / distance;
                    double collisionX = nx * collisionScale;
                    double collisionY = ny * collisionScale;

                    ball.speedX -= collisionX;
                    ball.speedY -= collisionY;
                    other.speedX += collisionX;
                    other.speedY += collisionY;

                }
                
            }
            
            // Check collision with bricks
            boolean hitBrick = false;
           // boolean hitFloor = false;
            for (int i1 = 0; i1 < bucket.map.length; i1++) {
                for (int j = 0; j < bucket.map[0].length; j++) {
                    if (bucket.map[i1][j] != null) {
                        int brickX = j * bucket.brickWidth + bucket.offsetX;
                        int brickY = i1 * bucket.brickHeight + bucket.offsetY;

                        Rectangle brickRect = new Rectangle(brickX, brickY, bucket.brickWidth, bucket.brickHeight);
                        Rectangle ballRect = new Rectangle(
                            (int) (ball.x - ball.radius),
                            (int) (ball.y - ball.radius),
                            ball.radius * 2,
                            ball.radius * 2
                        );
//add salt
                        if (ballRect.intersects(brickRect)) {
                            hitBrick = true;
                            // Update score
                            score += ball.value;

                            // Process ball's password
                            String[] ballParts = ball.getString().split("/");
                            int ballFirstNumber = Integer.parseInt(ballParts[0]);
                            int ballSecondNumber = Integer.parseInt(ballParts[1]);

                            String passwordEntry = processedPassword.get(j);
                            int passwordValue = Integer.parseInt(passwordEntry);
                            int sum = ballSecondNumber + passwordValue;

                            String newEntry = ballFirstNumber + "/" + j + "/" + sum;
                            
                            System.out.println("Last Entry: " + newEntry);
                            newest.add(newEntry);
                            System.out.println(newest);

                            break;
                        }
                   
                        
                    }
                }
                if (hitBrick) break;
            }

            if (hitBrick) {
                ballsToRemove.add(ball);
                continue;
            }
            
        }
        

        // Remove balls that hit bricks (existing logic)
        for (Ball ball : ballsToRemove) {
            ballManager.removeBall(ball);
        }
    }

    private void fileWriterLoop() {
        String zipFilePath = "output.zip";

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos);
             OutputStreamWriter osw = new OutputStreamWriter(zos);
             BufferedWriter writer = new BufferedWriter(osw)) {

            ZipEntry zipEntry = new ZipEntry("output.txt");
            zos.putNextEntry(zipEntry);

            while (running || !writeQueue.isEmpty()) {
                String entry = writeQueue.poll(100, TimeUnit.MILLISECONDS);
                if (entry != null) {
                    String[] parts = entry.split("/");
                    String middlePart = parts[1];
                    String lastPart = parts[2];

                    int lastNumber = Integer.parseInt(lastPart);
                    char utf8Char = (char) lastNumber;

                    String result = middlePart + "/" + utf8Char;

                    writer.write(result);
                    writer.newLine();
                }
            }

            zos.closeEntry();
            System.out.println("Transformed strings written to " + zipFilePath);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error in file writing: " + e.getMessage());
        }
    }
}
