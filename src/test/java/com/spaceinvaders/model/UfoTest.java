package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Headless checks of the mystery saucer: dashes, direction, despawn. */
class UfoTest {

    @Test
    void startsDormantAndInvisible() {
        Ufo ufo = new Ufo();
        assertFalse(ufo.active);
        assertFalse(ufo.node.isVisible(), "no saucer on screen before the first spawn");
    }

    @Test
    void spawnsFromTheRequestedEdge() {
        Ufo ufo = new Ufo();
        ufo.spawn(1);
        assertEquals(-Ufo.W, ufo.x, "rightward saucer enters from the left edge");
        assertTrue(ufo.active);
        assertTrue(ufo.node.isVisible());
        assertEquals(1, ufo.dir);

        ufo.spawn(-1);
        assertEquals(Constants.GAME_W, ufo.x, "leftward saucer enters from the right edge");
        assertEquals(-1, ufo.dir);
    }

    @Test
    void movesAtUfoSpeedWhileActive() {
        Ufo ufo = new Ufo();
        ufo.spawn(1);
        ufo.update(0.5);
        assertEquals(-Ufo.W + Constants.UFO_SPEED * 0.5, ufo.x, 1e-9);
    }

    @Test
    void exitsRightAndDespawns() {
        Ufo ufo = new Ufo();
        ufo.spawn(1);
        ufo.update(13); // far enough to cross the 224er-wide playfield
        assertTrue(ufo.x > Constants.GAME_W);
        assertFalse(ufo.active, "saucer gone off-screen");
        assertFalse(ufo.node.isVisible());
    }

    @Test
    void exitsLeftAndDespawns() {
        Ufo ufo = new Ufo();
        ufo.spawn(-1);
        ufo.update(13);
        assertTrue(ufo.x + Ufo.W < 0);
        assertFalse(ufo.active);
        assertFalse(ufo.node.isVisible());
    }

    @Test
    void despawnIsSafeToCallTwice() {
        Ufo ufo = new Ufo();
        ufo.spawn(1);
        ufo.despawn();
        ufo.despawn();
        assertFalse(ufo.active);
        assertFalse(ufo.node.isVisible());
    }
}
