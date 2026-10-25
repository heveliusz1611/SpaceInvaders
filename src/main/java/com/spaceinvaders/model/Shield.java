package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteData;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

import java.util.Random;

/**
 * One destructible bunker. It stores its own pixel-solidity grid, so shots
 * carve holes in it exactly like the 1978 DestroyableShield ROM cells.
 */
public final class Shield {
    public static final int W = 22;
    public static final int H = 16;

    private static final int BULLET_RADIUS = 2;
    private static final int BULLET_SCATTER = 2;
    private static final int BOMB_RADIUS = 3;
    private static final int BOMB_SCATTER = 4;

    private final boolean[][] solid = new boolean[H][W];
    private final Rectangle[][] pixels = new Rectangle[H][W];
    public final Group node = new Group();
    private final Random rng = new Random();
    /** World coordinates of the bunker's top-left pixel. */
    public final double x;
    public final double y;

    public Shield(double x, double y) {
        this.x = x;
        this.y = y;
        for (int row = 0; row < H; row++) {
            String line = SpriteData.SHIELD[row];
            for (int col = 0; col < W; col++) {
                solid[row][col] = line.charAt(col) == 'X';
                if (solid[row][col]) {
                    Rectangle pixel = new Rectangle(x + col, y + row, 1, 1);
                    pixel.setFill(Constants.PLAYER_GREEN);
                    pixels[row][col] = pixel;
                    node.getChildren().add(pixel);
                }
            }
        }
    }

    /**
     * Finds the first solid pixel a moving rectangle would bite into.
     * Returns {row, col} in bunker-local cells, or null when it misses.
     * A shot rising from below scans bottom-up; a bomb falling scans top-down.
     */
    public int[] hit(double rx, double ry, double rw, double rh, boolean fromBelow) {
        // Reject rectangles entirely outside the bunker before clamping,
        // otherwise the edge cells would swallow misses.
        double colA = Math.floor(rx - x);
        double colB = Math.floor(rx + rw - 1 - x);
        double rowA = Math.floor(ry - y);
        double rowB = Math.floor(ry + rh - 1 - y);
        if (colB < 0 || colA > W - 1 || rowB < 0 || rowA > H - 1) {
            return null;
        }
        int cA = clampCol((int) colA);
        int cB = clampCol((int) colB);
        int rA = clampRow((int) rowA);
        int rB = clampRow((int) rowB);
        int first = fromBelow ? rB : rA;
        int last = fromBelow ? rA : rB;
        int step = fromBelow ? -1 : 1;
        for (int row = first; row != last + step; row += step) {
            for (int col = cA; col <= cB; col++) {
                if (solid[row][col]) {
                    return new int[] {row, col};
                }
            }
        }
        return null;
    }

    /** Carves a blast crater (clear disc plus scattered stray pixels). */
    public void blast(int row, int col, int radius, int scatter) {
        for (int dr = -radius; dr <= radius; dr++) {
            for (int dc = -radius; dc <= radius; dc++) {
                if (dr * dr + dc * dc <= radius * radius + 1) {
                    clear(row + dr, col + dc);
                }
            }
        }
        for (int i = 0; i < scatter; i++) {
            int dr = rng.nextInt(2 * (radius + 1) + 1) - (radius + 1);
            int dc = rng.nextInt(2 * (radius + 1) + 1) - (radius + 1);
            if (dr * dr + dc * dc <= radius * radius + 1) {
                clear(row + dr, col + dc);
            }
        }
    }

    /** Carves the crater a cannon shot leaves in this bunker (bottom-up). */
    public void bulletImpact(int hitRow, int hitCol) {
        blast(hitRow, hitCol, BULLET_RADIUS, BULLET_SCATTER);
    }

    /** Carves the crater an alien bomb leaves in this bunker (top-down). */
    public void bombImpact(int hitRow, int hitCol) {
        blast(hitRow, hitCol, BOMB_RADIUS, BOMB_SCATTER);
    }

    private void clear(int row, int col) {
        if (row < 0 || row >= H || col < 0 || col >= W || !solid[row][col]) {
            return;
        }
        solid[row][col] = false;
        Rectangle pixel = pixels[row][col];
        pixel.setVisible(false);
    }

    private int clampCol(int col) {
        return Math.clamp(col, 0, W - 1);
    }

    private int clampRow(int row) {
        return Math.clamp(row, 0, H - 1);
    }
}
