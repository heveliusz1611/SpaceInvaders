package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Headless checks of the cannon: spawn point, speed clamps, center line. */
class PlayerTest {

    private static final double W = Player.W;

    @Test
    void spawnsCenteredOnTheFloor() {
        Player player = new Player();
        assertEquals((Constants.GAME_W - W) / 2.0, player.x, 1e-9);
        assertEquals((Constants.GAME_W - W) / 2.0 + W / 2.0, player.centerX(), 1e-9);
        assertEquals(Constants.PLAYER_Y, player.node.getTranslateY());
        assertTrue(player.node.isVisible(), "the cannon ships visible");
    }

    @Test
    void moveTracksSpeedTimesDelta() {
        Player player = new Player();
        player.move(10);
        assertEquals(105.5 + 10, player.x, 1e-9);
        player.move(-3.5);
        assertEquals(112, player.x, 1e-9);
    }

    @Test
    void neverLeavesThePlayfield() {
        Player player = new Player();
        player.move(-1000);
        assertEquals(4, player.x, "clamped to the left margin");
        assertEquals(4, player.node.getTranslateX());
        player.move(+1000);
        assertEquals(Constants.GAME_W - W - 4, player.x, "clamped to the right margin");
        assertEquals(Math.round(Constants.GAME_W - W - 4), player.node.getTranslateX());
    }

    @Test
    void respawnReturnsTheCannonToCenter() {
        Player player = new Player();
        player.move(-1000);
        player.move(+1000);
        player.respawn();
        assertEquals((Constants.GAME_W - W) / 2.0, player.x, 1e-9);
        assertEquals(Math.round((Constants.GAME_W - W) / 2.0), player.node.getTranslateX());
    }
}
