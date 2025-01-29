package generators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class PegGenerator {
    private List<Peg> pegs; // List of pegs
    private int rows; // Number of rows in the pyramid
    private int pegRadius; // Radius of each peg
    private int offsetX; // Horizontal offset for the pyramid
    private int offsetY; // Vertical offset for the pyramid 
    private int spacing; // Spacing between pegs

    public PegGenerator(int rows, int pegRadius, int offsetX, int offsetY, int spacing) {
        this.rows = rows;
        this.pegRadius = pegRadius;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.spacing = spacing;
        this.pegs = new ArrayList<>();
        generatePyramid();
    }

    // Generate the pegs in a pyramid formation with three pegs at the top
    private void generatePyramid() {
        for (int row = 0; row < rows; row++) {
            int numPegs = 3 + row; // Start with 3 pegs and increase by 1 each row
            int rowY = offsetY + row * spacing; // Y-coordinate of the current row

            // Center the row horizontally
            int rowStartX = offsetX - (numPegs - 1) * spacing / 2;

            for (int col = 0; col < numPegs; col++) {
                int pegX = rowStartX + col * spacing;
                pegs.add(new Peg(pegX, rowY, pegRadius));
            }
        }
    }

    // Get a list of all pegs
    public List<Peg> getPegs() {
        return pegs;
    }

    // Draw the pegs
    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        for (Peg peg : pegs) {
            g.fillOval(peg.x - peg.radius, peg.y - peg.radius, peg.radius * 2, peg.radius * 2);
        }
    }

    // Inner class to represent a single peg
    public static class Peg {
        private int x, y, radius;

        public Peg(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        // Create a Rectangle for collision detection
        public Rectangle getBounds() {
            return new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
        }

        // Get the center X-coordinate of the peg
        public float getCenterX() {
            return x;
        }

        // Get the center Y-coordinate of the peg
        public float getCenterY() {
            return y;
        }

        public int getRadius() {
            return radius;
        }
    }
}
