package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure model of the 11x5 invader formation - no JavaFX nodes, so the pace,
 * stepping and bounds logic can be unit-tested headlessly.
 */
public final class AlienGrid {
    public enum Move { LEFT, RIGHT, DROP_TURN }

    private final boolean[][] alive = new boolean[Constants.GRID_ROWS][Constants.GRID_COLS];
    private int direction = -1;
    private double tx;
    private double ty;

    public AlienGrid() {
        reset();
    }

    /** Restores the full 11x5 formation at its march-off starting position. */
    public void reset() {
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                alive[row][col] = true;
            }
        }
        tx = 0;
        ty = 0;
        direction = -1;
    }

    public int aliveCount() {
        int count = 0;
        for (boolean[] row : alive) {
            for (boolean cell : row) {
                if (cell) count++;
            }
        }
        return count;
    }

    public boolean isAlive(int row, int col) {
        return alive[row][col];
    }

    public void kill(int row, int col) {
        alive[row][col] = false;
    }

    /** Seconds between formation steps for the given live count (classic speed-up). */
    public static double stepInterval(int aliveCount) {
        double ratio = Math.max(0.0, Math.min(1.0, aliveCount / (double) (Constants.GRID_ROWS * Constants.GRID_COLS)));
        return 0.03 + 0.155 * Math.pow(ratio, 1.7);
    }

    /**
     * Advances the formation by one step: {@link Constants#STEP_X} sideways, or
     * {@link Constants#STEP_DOWN} down plus a direction turn at an edge.
     */
    public Move step() {
        double left = Double.MAX_VALUE;
        double right = -Double.MAX_VALUE;
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            AlienKind kind = AlienKind.forRow(row);
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (!alive[row][col]) continue;
                double lx = col * Constants.CELL_W + (Constants.CELL_W - kind.width()) / 2.0;
                left = Math.min(left, lx);
                right = Math.max(right, lx + kind.width());
            }
        }
        if (aliveCount() == 0) {
            return Move.LEFT;
        }
        double worldLeft = left + Constants.GRID_START_X + tx;
        double worldRight = right + Constants.GRID_START_X + tx;
        if (direction > 0 && worldRight + Constants.STEP_X > Constants.GAME_W - Constants.GRID_MARGIN) {
            ty += Constants.STEP_DOWN;
            direction = -1;
            return Move.DROP_TURN;
        }
        if (direction < 0 && worldLeft - Constants.STEP_X < Constants.GRID_MARGIN) {
            ty += Constants.STEP_DOWN;
            direction = 1;
            return Move.DROP_TURN;
        }
        tx += direction * Constants.STEP_X;
        return direction > 0 ? Move.RIGHT : Move.LEFT;
    }

    /** Bottom edge (Y, formation-local game pixels) of the lowest live invader. */
    public double bottomLocalY() {
        double bottom = 0;
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            AlienKind kind = AlienKind.forRow(row);
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (alive[row][col]) {
                    bottom = Math.max(bottom, (row + 1) * Constants.CELL_H);
                }
            }
        }
        return bottom;
    }

    /**
     * Picks a uniformly random live column and returns the bottom-most live
     * invader in it: {row, col}, or null when the formation is empty.
     */
    public int[] randomBottomAlien(Random rng) {
        List<Integer> liveColumns = new ArrayList<>();
        for (int col = 0; col < Constants.GRID_COLS; col++) {
            for (int row = Constants.GRID_ROWS - 1; row >= 0; row--) {
                if (alive[row][col]) {
                    liveColumns.add(col);
                    break;
                }
            }
        }
        if (liveColumns.isEmpty()) {
            return null;
        }
        int col = liveColumns.get(rng.nextInt(liveColumns.size()));
        for (int row = Constants.GRID_ROWS - 1; row >= 0; row--) {
            if (alive[row][col]) {
                return new int[] {row, col};
            }
        }
        return null;
    }

    public double getTx() {
        return tx;
    }

    public double getTy() {
        return ty;
    }
}
