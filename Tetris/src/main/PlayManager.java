package main;

import tetromino.Block;
import tetromino.Tetromino;
import tetromino.mino.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class PlayManager {

    // main.Main area
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    // Tetromino
    Tetromino currentMino;
    final int TETROMINO_START_X;
    final int TETROMINO_START_Y;
    Tetromino nextTetromino;
    final int NEXTTETROMINO_X;
    final int NEXTTETROMINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();

    // Misc
    public static int dropInterval = 60; // Tetromino drops in every 60 Frames
    boolean gameOver;

    // Effects
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();

    // Level / Score
    int level = 1;
    int lines;
    int score;

    public PlayManager() {

        // main.Main area frame
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2); // 1280/2 - 360/2 = 460
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;

        TETROMINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        TETROMINO_START_Y = top_y + Block.SIZE;

        // Next Tetromino
        NEXTTETROMINO_X = right_x + 175;
        NEXTTETROMINO_Y = top_y + 500;

        // Set the starting Tetromino
        currentMino = pickTetromino();
        currentMino.setXY(TETROMINO_START_X, TETROMINO_START_Y);
        nextTetromino = pickTetromino();
        nextTetromino.setXY(NEXTTETROMINO_X, NEXTTETROMINO_Y);
    }

    private Tetromino pickTetromino() {
        // Pick a random tetromino
        Tetromino tetromino = null;
        int i = new Random().nextInt(7);

        switch(i) {
            case 0: tetromino = new Tetromino_L1(); break;
            case 1: tetromino = new Tetromino_L2(); break;
            case 2: tetromino = new Tetromino_Square(); break;
            case 3: tetromino = new Tetromino_Bar(); break;
            case 4: tetromino = new Tetromino_T(); break;
            case 5: tetromino = new Tetromino_Z1(); break;
            case 6: tetromino = new Tetromino_Z2(); break;
        }

        return tetromino;
    }

    public void update() {

        // Check if the currentMino is active
        if (currentMino.active == false) {
            // If the Tetromino is not active, put it into the staticBlocks
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);

            // Check if gameOver
            if (currentMino.b[0].x == TETROMINO_START_X && currentMino.b[0].y == TETROMINO_START_Y) {
                // It means the currentMino immediately collided w/ a block and couldn't move at all.
                // It's XY are the same with the nextTetromino's
                gameOver = true;
            }

            currentMino.deactivating = false;

            // Replace the currentMino with the nextTetromino
            currentMino = nextTetromino;
            currentMino.setXY(TETROMINO_START_X, TETROMINO_START_Y);
            nextTetromino = pickTetromino();
            nextTetromino.setXY(NEXTTETROMINO_X, NEXTTETROMINO_Y);

            // When a tetromino becomes inactive, check if (line(s) can be deleted)
            checkDeletion();
        } else {
            currentMino.update();
        }
    }

    public void checkDeletion() {
        int x = left_x;
        int y = top_y;
        int blockCount = 0;
        int lineCount = 0;

        while (x < right_x && y < bottom_y) {
            for (int i = 0; i < staticBlocks.size(); i++) {
                if (staticBlocks.get(i).x == x && staticBlocks.get(i).y == y) {
                    blockCount++;
                }
            }

            x += Block.SIZE;

            if (x == right_x) {
                // Block count hits 12, that means the current y-line is all filled with blocks
                // Delete them
                if (blockCount == 12) {

                    effectCounterOn = true;
                    effectY.add(y);

                    for (int i = staticBlocks.size() - 1; i > -1; i--) {
                        // Remove all the blocks in the current y-line
                        if (staticBlocks.get(i).y == y) {
                            staticBlocks.remove(i);
                        }
                    }

                    // Line Counter
                    lineCount++;
                    lines++;

                    // Dropping speed
                    // When the line score hits a certain number, increase the drop speed.
                    // 1 - fastest, 10 - slowest
                    if (lines % 10 == 0 && dropInterval > 1) {
                        level++;
                        if (dropInterval > 10) {
                            dropInterval -= 10;
                        } else {
                            dropInterval -= 1;
                        }
                    }

                    // After deletion of a line, slide down blocks that are above it.
                    for (int i = 0; i < staticBlocks.size(); i++) {
                        if (staticBlocks.get(i).y < y) {
                            staticBlocks.get(i).y += Block.SIZE;
                        }
                    }
                }

                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }

        // Adding score
        if (lineCount > 0) {
            int singleLineScore = 10 * level;
            score += singleLineScore * lineCount;
        }
    }

    public void draw(Graphics2D g2) {

        // Draw main play area
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        // Draw next mino frame
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x + 60, y + 60);

        // Draw Score Frame
        g2.drawRect(x, top_y, 250, 300);
        x += 40;
        y = top_y + 90;
        g2.drawString("LEVEL: " + level, x, y); y += 70;
        g2.drawString("LINES: " + lines, x, y); y += 70;
        g2.drawString("SCORE: " + score, x, y);

        // Draw the currentMino
        if (currentMino != null) {
            currentMino.draw(g2);
        }

        // Draw the nextTetromino
        nextTetromino.draw(g2);

        // Draw static blocks, inactive tetrominos
        for (int i = 0; i < staticBlocks.size(); i++) {
            staticBlocks.get(i).draw(g2);
        }

        // Draw Effect
        if (effectCounterOn) {
            effectCounter++;

            g2.setColor(Color.red);
            for (int i = 0; i < effectY.size(); i++) {
                g2.fillRect(left_x, effectY.get(i), WIDTH, Block.SIZE);
            }

            if (effectCounter == 15) {
                effectCounterOn = false;
                effectCounter = 0;
                effectY.clear();
            }
        }

        // Draw Pause
        g2.setColor(Color.YELLOW);
        g2.setFont(g2.getFont().deriveFont(50f));
        // GameOver
        if (gameOver) {
            x = left_x + 25;
            y = top_y + 320;
            g2.drawString("GAME OVER", x, y);
        } else if (KeyHandler.pausePressed) {
            x = left_x + 70;
            y = top_y + 320;
            g2.drawString("PAUSED", x, y);
        }

        // Game Title
        x = 35;
        y = top_y + 320;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Times New Roman", Font.ITALIC, 60));
        g2.drawString("Tetris in Java", x + 20, y);
    }
}
