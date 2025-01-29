package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GameRenderer extends JPanel {
    private static final int BORDER_WIDTH = 3;
    private static final int GAME_WIDTH = 692;
    private static final int GAME_HEIGHT = 592;
    private static final Font SCORE_FONT = new Font("serif", Font.BOLD, 25);
    private static final Font GAME_OVER_FONT = new Font("serif", Font.BOLD, 30);
    private static final Font RESTART_FONT = new Font("serif", Font.BOLD, 20);

    private BucketGenerator map;
    private BallManager ballManager;
    private int score;
    private boolean play;
    private CollisionDetector collisionDetector;
    private PegGenerator pegGenerator;

    public GameRenderer(BucketGenerator map, BallManager ballManager, CollisionDetector collisionDetector, PegGenerator pegGenerator) {
        this.map = map;
        this.ballManager = ballManager;
        this.collisionDetector = collisionDetector;
        this.pegGenerator = pegGenerator; // Initialize here
        this.score = 0;
        this.play = true;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Draw bricks
        map.draw((Graphics2D) g);

        // Draw pegs
        pegGenerator.draw((Graphics2D) g);

        // Draw borders
        drawBorders(g);

        // Display score
        g.setColor(Color.WHITE);
        g.setFont(SCORE_FONT);
        g.drawString(String.valueOf(score), GAME_WIDTH - 100, 30);

        // Draw balls
        for (Ball ball : ballManager.getActiveBalls()) {
            g.setColor(ball.color);
            g.fillOval((int) (ball.x - ball.radius), (int) (ball.y - ball.radius), ball.radius * 2, ball.radius * 2);
        }

        // Game over/win screen
        if (!ballManager.hasPendingBalls() && ballManager.getActiveBalls().isEmpty()) {
            play = false;
            g.setColor(Color.RED);
            g.setFont(GAME_OVER_FONT);
            g.drawString("You Won!", 260, 300);
            g.setFont(RESTART_FONT);
            g.drawString("Press Enter to Restart", 230, 350);
        }

        g.dispose();
    }

    private void drawBorders(Graphics g) {
        g.setColor(Color.YELLOW);
        // Left, Top, Right, Bottom borders
        g.fillRect(0, 0, BORDER_WIDTH, GAME_HEIGHT); // Left
        g.fillRect(0, 0, GAME_WIDTH, BORDER_WIDTH); // Top
        g.fillRect(GAME_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, GAME_HEIGHT); // Right
        g.fillRect(0, GAME_HEIGHT - BORDER_WIDTH, GAME_WIDTH, BORDER_WIDTH); // Bottom
    }

    public void updateScore(int newScore) {
        this.score = newScore;
    }

    public boolean isPlay() {
        return play;
    }
}
