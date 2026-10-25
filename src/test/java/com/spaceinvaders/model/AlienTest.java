package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Headless checks of a single invader: frames, death, wave reload, placement. */
class AlienTest {

    @Test
    void spawnsAliveOnItsFirstMarchFrame() {
        Alien alien = new Alien(0, 0);
        assertTrue(alien.isAlive());
        assertTrue(alien.node.isVisible());
        assertTrue(alien.node.getChildren().get(0).isVisible(), "frame A shown");
        assertFalse(alien.node.getChildren().get(1).isVisible(), "frame B hidden");
    }

    @Test
    void formationPickstheRightKindPerRow() {
        assertEquals(AlienKind.SQUID, new Alien(0, 0).kind);
        assertEquals(AlienKind.OCTOPUS, new Alien(4, 0).kind);
        assertEquals(AlienKind.CRAB, new Alien(2, 10).kind);
    }

    @Test
    void animFramesAlternateOnEveryStep() {
        Alien alien = new Alien(0, 0);
        alien.toggleFrame();
        assertFalse(alien.node.getChildren().get(0).isVisible());
        assertTrue(alien.node.getChildren().get(1).isVisible());
        alien.toggleFrame();
        assertTrue(alien.node.getChildren().get(0).isVisible());
        assertFalse(alien.node.getChildren().get(1).isVisible());
    }

    @Test
    void killHidesTheSpriteForGood() {
        Alien alien = new Alien(0, 0);
        alien.kill();
        assertFalse(alien.isAlive());
        assertFalse(alien.node.isVisible(), "a killed invader must vanish from the screen");
        alien.toggleFrame(); // dead invaders no longer animate, and stay hidden
        assertFalse(alien.node.isVisible());
    }

    @Test
    void resetRevivesInvisibleAgain() {
        Alien alien = new Alien(0, 0);
        alien.kill();
        alien.reset();
        assertTrue(alien.isAlive());
        assertTrue(alien.node.isVisible(), "a reloaded wave must show invaders again");
        assertTrue(alien.node.getChildren().get(0).isVisible(), "back to frame A");
        assertFalse(alien.node.getChildren().get(1).isVisible());
    }

    @Test
    void spritesSitInTheirFormationCells() {
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            Alien alien = new Alien(row, 3);
            AlienKind kind = alien.kind;
            double expectedX = 3 * Constants.CELL_W + (Constants.CELL_W - kind.width()) / 2.0;
            double expectedY = row * Constants.CELL_H;
            assertEquals(expectedX, alien.localX(), 1e-9, "centered inside its 16px cell");
            assertEquals(expectedY, alien.localY(), 1e-9, "stacked one cell apart");
        }
    }
}
