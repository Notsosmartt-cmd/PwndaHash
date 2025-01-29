package proccessors; 
// Specifies the package name for organizing the Ball class into the "test" package.

import java.awt.Color;
// Imports the Color class from the java.awt package, which is used to represent the color of the ball.
import java.util.List;

public class Ball {
    // Defines a public class named Ball, representing a ball object with properties like position, speed, size, and color.

    public float x, y; //Location of ball
    public float speedX, speedY; //Speed of ball
    public int radius;    // Represents the radius of the ball.
    public Color color;  // Represents the color of the ball using the Color class.
    int filePosition; // Position in the file
    int utf8Value; // UTF-8 value of the character at that position
    String string;
    public int value;

    public Ball(float x, float y, float speedX, float speedY, int radius, Color color, int value, String string) {
        // Constructor for the Ball class. Initializes the ball's properties when a new Ball object is created.
       
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.radius = radius;
        this.color = color; 
        this.value = value;
        this.utf8Value = utf8Value;
        this.filePosition = filePosition;
        this.string = string;
    }
 
    // Optionally add getters for filePosition and utf8Value if needed
    public int getFilePosition() {
        return filePosition;
    }

    public int getUtf8Value() {
        return utf8Value;
    }
    
    public String getString() {
    	return string;
    }
    
    
    
}
