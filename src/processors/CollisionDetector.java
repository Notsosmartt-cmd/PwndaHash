package processors;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import generators.BucketGenerator;
import generators.PegGenerator;
import main.Gameplay;
import processors.processPassword;

public class CollisionDetector {
    private BucketGenerator bucket;
    private BallManager ballManager;
    private int score;
    private volatile boolean running; // To control thread execution
    private List<String> newest; // Shared list for collision data
    private List<PegGenerator.Peg> pegs;
    private String inputFilePath;
    
    
    public CollisionDetector(BucketGenerator map, BallManager ballManager, int score, String password,List<PegGenerator.Peg> pegs, String inputFilePath) {
        this.bucket = map;
        this.ballManager = ballManager;
        this.score = score;
        this.pegs = pegs;
        this.newest = new CopyOnWriteArrayList<>(); // Thread-safe list
        this.running = true; // Threads will run until explicitly stopped
        this.inputFilePath = inputFilePath;
    }

    public void startThreads() {
        // Thread for collision detection
        Thread collisionThread = new Thread(() -> {
            while (running) {
                detectCollisions();
                try {
                    Thread.sleep(10); // Control collision detection frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Thread for processing and writing
        Thread writeThread = new Thread(() -> {
            while (running) {
                sortAndTransform();
                try {
                    Thread.sleep(3000); // Control write frequency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        collisionThread.setPriority(Thread.NORM_PRIORITY + 1);  // Higher priority for collision thread
        writeThread.setPriority(Thread.NORM_PRIORITY - 1);     // Lower priority for write thread

        collisionThread.start();
        writeThread.start();
    }

    public void stopThreads() {
        running = false;
    }
    
    public int getScore() {
        return score;
    }

    public void detectCollisions() {
        List<Ball> ballsToRemove = new ArrayList<>();
        Random random = new Random();
        
        List<String> processedPassword =processPassword.processPassword(Gameplay.password);
        List<Ball> activeBalls = ballManager.getActiveBalls();
        
        int[] passwordValArray = new int[processedPassword.size()]; // Create an integer array with the same size

        // Convert each String element to an integer and store in the array
        for (int i = 0; i < processedPassword.size(); i++) {
            passwordValArray[i] = Integer.parseInt(processedPassword.get(i));
            if (Character.isSupplementaryCodePoint(passwordValArray[i])) {
                i++; // Skip the second char in the surrogate pair
            }
        }
        
     // Retrieve the last element of the array
        int salt = passwordValArray[passwordValArray.length - 1];
        //System.out.println("Last element of the array: " + salt);
        

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
                int sum = ballSecondNumber + passwordValue + salt;
                char utf8Char = (char) sum;

                // Create a new password entry with updated values
                String newEntry = ballFirstNumber + "/" + passwordIndex + "/" + utf8Char;
                
                // Use synchronized block to safely modify shared resource
                synchronized (newest) {
                    newest.add(newEntry);
                }

                System.out.println("Ball hit the floor and was removed. New password entry: " + newEntry);
            }

            // Remove balls that hit bricks (existing logic)
            boolean hitBrick = false;
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
                            int sum = ballSecondNumber + passwordValue + salt;
                            char utf8Char = (char) sum;
                            
                            String newEntry = ballFirstNumber + "/" + j + "/" + utf8Char;
                            System.out.println("Last Entry: " + newEntry);
                            
                            // Safely modify shared resource
                            synchronized (newest) {
                                newest.add(newEntry);
                            }

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

        // Remove balls that hit bricks
        for (Ball ball : ballsToRemove) {
            ballManager.removeBall(ball);
        }
    }

    public void sortAndTransform() {
        
    	synchronized (newest) {
            if (!newest.isEmpty()) {
            //maybe use linked list
            	// Get the directory of the input file
                Path inputFilePathObj = Paths.get(inputFilePath);
                Path inputFileDir = inputFilePathObj.getParent();
            	
             // Construct the path for entry.txt in the same directory as the input file
                Path entryFilePath = inputFileDir.resolve("entry.txt");
            	
                // Write and clear data from the newest list
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(entryFilePath.toFile(), true))) {
                	
                    while (!newest.isEmpty()) {
                        String entry = newest.remove(0);
                        writer.write(entry + System.lineSeparator());
                        System.out.println("Temp entry written");
                    }
                    System.out.println("Transformed strings written directly to entry.txt");
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }
        }

        
    	// Get the directory of the input file
        Path inputFilePathObj = Paths.get(inputFilePath);
        Path inputFileDir = inputFilePathObj.getParent();  // Directory of the input file
        String inputFileName = inputFilePathObj.getFileName().toString();  // This will store the file name (e.g., "example.txt")
        // Construct the path for output.txt in the same directory as the input file
        Path outputFilePath = inputFileDir.resolve("(Pwnda)" + inputFileName);
        
        
        // Construct the path for entry.txt in the same directory as the input file
        Path entryFilePath = inputFileDir.resolve("entry.txt");
        
        // Read, sort, and transform the output.txt content
       
     // Read, sort, and transform the output.txt content
        
        try {
            // Read all lines from entry.txt
            List<String> lines = Files.readAllLines(entryFilePath);

            // Parse and sort by the first integer in each line
            lines.sort((a, b) -> {
                int firstA = Integer.parseInt(a.split("/")[0]);
                int firstB = Integer.parseInt(b.split("/")[0]);
                return Integer.compare(firstA, firstB);
            });

            /* When using big password the program generates the buckets really small (sometimes 1 pixel wide), so the ball can hit multiple
            	pixels at once. The program tends to just process the ball twice if it hits two buckets at once, so the first integer (which is the ball's location
            	in the file) gets put into a hash set and any duplicates of the first integer just gets deleted since the first segment hasn't been touched since 
            	it was created in ball manager
            */
            // Remove lines with duplicate first integers
            List<String> uniqueLines = new ArrayList<>();
            Set<Integer> seenFirstIntegers = new HashSet<>(); //We use the logic of the hashset to know if we should add it to uniqueLines array

            for (String line : lines) {
                String[] parts = line.split("/", 2);
                if (parts.length > 1) {
                    int firstInteger = Integer.parseInt(parts[0]); //Isolates the first integer in the line
                    
                    if (seenFirstIntegers.add(firstInteger)) { // Add to set if not already present
                        uniqueLines.add(line); // Keep the line if it's the first occurrence
                    }
                    
                }
            }

            // Remove the first integer and its divider from each line
            List<String> transformedLines = new ArrayList<>();
            for (String line : uniqueLines) {
                String[] parts = line.split("/", 2);
                if (parts.length > 1) {
                    transformedLines.add(parts[1]); // Keep everything after the first integer and '/'
                }
            }

            // Rewrite the transformed lines back to output.txt
            Files.write(outputFilePath, transformedLines);
            System.out.println("Sorted, deduplicated, and transformed lines written back to " + outputFilePath);

            // Delete the entry.txt file
            if (Files.deleteIfExists(entryFilePath)) {
                System.out.println("entry.txt has been deleted");
            } else {
                System.out.println("entry.txt does not exist or could not be deleted");
            }

        } catch (IOException e) {
            System.err.println("Error during file operations: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing integers: " + e.getMessage());
        }
        
       
    }
    }
