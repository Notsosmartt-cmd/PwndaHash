
package generators;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import processors.processPassword;

public class BucketGenerator {
    // 2D array to represent the bricks
    public String[][] map;
    public int brickWidth;  // Width of each brick
    public int brickHeight; // Height of each brick
    public int offsetX;     // Horizontal offset for the brick grid
    public int offsetY;     // Vertical offset for the brick grid
    processPassword processor = new processPassword();
    // Constructor initializes the map based on the password length
    public BucketGenerator(String password, int gameWidth, int gameHeight) {
        // Limit password length to 512
        if (password.length() > 512) {
            System.out.println("Password exceeds 512 characters. Truncating to 512 characters.");
            password = password.substring(0, 512); // Truncate password
        }

        int rows = 1;
        int cols = password.length();

        map = new String[rows][cols];
        //List<String> passwordData = processPassword(password);
        
        List<String> passwordData = processPassword.processPassword(password);
        
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = passwordData.get(j);
            }
        }

        // Calculate brick dimensions dynamically
       brickWidth = (gameWidth / cols) ;  // Divide game width by number of columns
     

        brickHeight = 40; // Fixed height or calculate based on rows if multiple rows are needed
        

        // Calculate offsets to center the grid
        offsetX = (gameWidth - (brickWidth * cols)) / 2;
        offsetY = gameHeight - 75; // Adjust vertical offset for proper alignment
    }

    
    // Draws the bricks on the screen
    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] != null) { // Only draw active bricks(non-null)
                    // Calculate the position of each brick
                    int brickX = j * brickWidth + offsetX;
                    int brickY = i * brickHeight + offsetY;

                    // Draw the brick with a white fill
                    g.setColor(Color.white);
                    g.fillRect(brickX, brickY, brickWidth, brickHeight);

                    // Draw the border of the brick
                    g.setStroke(new BasicStroke(3));
                    g.setColor(Color.black);
                    g.drawRect(brickX, brickY, brickWidth, brickHeight);
                }
            }
        }
    }
}
