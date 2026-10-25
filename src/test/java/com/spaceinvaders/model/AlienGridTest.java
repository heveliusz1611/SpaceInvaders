package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

/** Headless checks of the invader formation model. */
class AlienGridTest {

    @Test
    void freshGridIsFull() {
        AlienGrid grid = new AlienGrid();
        assertEquals(55, grid.aliveCount());
        assertEquals(0, grid.getTx());
        assertEquals(0, grid.getTy());
    }

    @Test
    void killReducesCount() {
        AlienGrid grid = new AlienGrid();
        grid.kill(0, 0);
        grid.kill(4, 10);
        assertEquals(53, grid.aliveCount());
    }

    @Test
    void stepsSidewaysThenDropsAndTurns() {
        AlienGrid grid = new AlienGrid();
        java.util.EnumSet<AlienGrid.Move> seen = java.util.EnumSet.noneOf(AlienGrid.Move.class);
        int steps = 0;
        while (seen.size() < 3 && steps < 500) {
            AlienGrid.Move move = grid.step();
            seen.add(move);
            steps++;
        }
        assertTrue(seen.contains(AlienGrid.Move.LEFT),
                "should march left first: " + seen);
        assertTrue(seen.contains(AlienGrid.Move.DROP_TURN),
                "should eventually hit the edge: " + seen);
        assertTrue(seen.contains(AlienGrid.Move.RIGHT),
                "should march right after the turn: " + seen);
        assertFalse(grid.getTy() <= 0, "should have dropped down at an edge");
    }

    @Test
    void keepsMarchingAndDroppingAcrossTheScreen() {
        AlienGrid grid = new AlienGrid();
        int drops = 0;
        int steps = 0;
        while (drops < 2 && steps < 500) {
            if (grid.step() == AlienGrid.Move.DROP_TURN) {
                drops++;
            }
            steps++;
        }
        assertEquals(2, drops, "should cross the screen twice in 500 steps");
        assertEquals(2 * Constants.STEP_DOWN, grid.getTy(), "two drops of 8px each");
    }

    @Test
    void intervalSpeedsUpAsAliensDie() {
        double full = AlienGrid.stepInterval(55);
        double lone = AlienGrid.stepInterval(1);
        assertTrue(full > lone, "fewer aliens should mean faster stepping");
        assertTrue(lone >= 0.03, "floor on step interval");
    }

    @Test
    void randomBottomAlienCoversLiveColumns() {
        AlienGrid grid = new AlienGrid();
        Random rng = new Random(42);
        boolean[] columnsSeen = new boolean[Constants.GRID_COLS];
        for (int i = 0; i < 500; i++) {
            int[] spot = grid.randomBottomAlien(rng);
            assertNotNull(spot);
            assertEquals(4, spot[0], "full grid: bottom row is row 4");
            columnsSeen[spot[1]] = true;
        }
        for (boolean seen : columnsSeen) {
            assertTrue(seen, "every column should be selectable");
        }
        for (int col = 0; col < Constants.GRID_COLS; col++) {
            grid.kill(4, col);
        }
        int[] spot = grid.randomBottomAlien(rng);
        assertNotNull(spot, "should still have a live column after clearing row 4");
        assertEquals(3, spot[0], "second row from the bottom once row 4 is empty");
    }

    @Test
    void emptyGridReturnsNoSpawner() {
        AlienGrid grid = new AlienGrid();
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                grid.kill(row, col);
            }
        }
        assertNull(grid.randomBottomAlien(new Random(1)));
    }
}
