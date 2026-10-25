package com.spaceinvaders.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.model.Alien;
import com.spaceinvaders.model.AlienGrid;
import com.spaceinvaders.model.Player;
import com.spaceinvaders.model.Projectile;
import com.spaceinvaders.model.Ufo;
import com.spaceinvaders.testutil.FxToolkit;
import com.spaceinvaders.testutil.Reflect;

import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

/**
 * Drives GamePanel's private update(dt) game loop headlessly, key state
 * injected straight into the KeyInput buffers: starting a game, pausing,
 * steering and firing, the invader march, the mystery saucer, wave reload,
 * death/respawn/game-over, bunker carving and effect-ttl housekeeping.
 */
class GamePanelLoopTest {

    private GamePanel panel;
    private Group content;
    private List<Projectile> bullets;
    private List<Projectile> bombs;

    @BeforeAll
    static void bootFx() {
        FxToolkit.ensure();
    }

    // ------------------------------------------------------------ harness

    private void spawnPanel() {
        panel = new GamePanel();
        content = Reflect.getAs(panel, "content");
        bullets = Reflect.getAs(panel, "bullets");
        bombs = Reflect.getAs(panel, "bombs");
    }

    /** Injects a one-shot press (the KeyInput "pressed" buffer). */
    private void press(KeyCode... keys) {
        Set<KeyCode> buffer = keyed("pressed");
        buffer.clear();
        buffer.addAll(List.of(keys));
    }

    /** Marks keys as continuously held (the KeyInput "down" set). */
    private void hold(KeyCode... keys) {
        Set<KeyCode> down = keyed("down");
        down.clear();
        down.addAll(List.of(keys));
    }

    private Set<KeyCode> keyed(String setField) {
        return Reflect.getAs(Reflect.getAs(panel, "input"), setField);
    }

    /** One pump of the real game loop. */
    private void update(double dt) {
        Reflect.invoke(panel, "update", new Class<?>[] {double.class}, dt);
    }

    String state() {
        return String.valueOf(Reflect.get(panel, "state"));
    }

    /** From the title screen straight into wave 1. */
    private void start() {
        press(KeyCode.ENTER);
        update(0.001);
        assertEquals("PLAYING", state(), "ENTER starts the game");
        press(); // release the one-shot so later ticks don't re-trigger
    }

    private void dropTrackedProjectiles() {
        for (Projectile shot : new ArrayList<>(bullets)) {
            content.getChildren().remove(shot.node);
        }
        for (Projectile shot : new ArrayList<>(bombs)) {
            content.getChildren().remove(shot.node);
        }
        bullets.clear();
        bombs.clear();
    }

    private void assertWaveMessage(String expected) {
        Text waveMsg = Reflect.getAs(panel, "waveMsg");
        assertEquals(expected, waveMsg.getText());
        assertTrue(waveMsg.isVisible(), "the wave banner shows during the transition");
    }

    // ------------------------------------------------------------ tests

    @Test
    void enterStartsTheGameAndTheFormationMarches() {
        spawnPanel();
        start();
        assertWaveMessage("WAVE 1");

        Group gridRoot = Reflect.getAs(panel, "gridRoot");
        double xBefore = gridRoot.getTranslateX();

        // enough simulated seconds for a few formation steps
        for (int tick = 0; tick < 10; tick++) {
            update(0.1);
        }
        int marchStep = (int) Reflect.get(panel, "marchStep");
        assertTrue(marchStep >= 1, "the four-note march beat plays per formation step");

        assertTrue(gridRoot.getTranslateX() < xBefore,
                "the formation group was re-positioned by the leftward march");
    }

    @Test
    void pKeyTogglesPauseAndFreezesTheLoop() {
        spawnPanel();
        start();

        press(KeyCode.P);
        update(0.016);
        assertTrue((boolean) Reflect.get(panel, "paused"), "P while playing pauses");
        assertTrue(Reflect.<Group>getAs(panel, "pauseOverlay").isVisible(),
                "the pause banner shows");

        Group gridRoot = Reflect.getAs(panel, "gridRoot");
        double yBefore = gridRoot.getTranslateY();
        press(); // no new presses while paused
        update(0.5); // a long frozen tick
        assertEquals(yBefore, gridRoot.getTranslateY(), "paused playfield does not advance");

        press(KeyCode.P);
        update(0.016);
        assertFalse((boolean) Reflect.get(panel, "paused"), "P again resumes");
        assertFalse(Reflect.<Group>getAs(panel, "pauseOverlay").isVisible());
    }

    @Test
    void arrowKeysSteerTheCannonAndSpaceFires() {
        spawnPanel();
        start();

        Player player = Reflect.getAs(panel, "player");
        double x0 = player.x;
        hold(KeyCode.LEFT, KeyCode.A);
        update(0.1);
        assertTrue(player.x < x0, "left keys steer the cannon left");

        double x1 = player.x;
        hold(KeyCode.RIGHT, KeyCode.D);
        update(0.1);
        assertTrue(player.x > x1, "right keys steer the cannon right");

        hold(KeyCode.SPACE);
        press(); // fire is trigger-held, not one-shot
        update(0.016);
        assertEquals(1, bullets.size(), "SPACE with an empty magazine fires");
        assertEquals(1, (int) Reflect.get(panel, "totalShots"), "exactly one round fired");

        // the pulse flies up and out the top, cleaning itself up in-flight
        for (int tick = 0; tick < 20 && !bullets.isEmpty(); tick++) {
            update(0.05);
        }
        assertTrue(bullets.isEmpty(), "the pulse left the playfield and the magazine");
    }

    @Test
    void mysterySaucerSpawnsAndPaysClassicPoints() {
        spawnPanel();
        start();
        Ufo ufo = Reflect.getAs(panel, "ufo");

        Reflect.set(panel, "ufoTimer", 0.0);
        update(0.016);
        assertTrue(ufo.active, "the timer expiry launches the saucer");
        assertTrue(ufo.node.isVisible(), "the saucer sprite shows");

        // the classic mystery rule: every 15th shot drops the 300-point saucer
        // (fire() bumps totalShots before the pulse flies, so a 15th shot hit reads 15)
        Reflect.set(panel, "totalShots", 15);
        bullets.add(Projectile.bullet(ufo.x, Constants.UFO_Y + 2));
        update(0.016);
        assertFalse(ufo.active, "the hit saucer goes down");
        assertEquals(300, (int) Reflect.get(panel, "score"), "the 15th shot pays 300");

        update(0.016); // the warble-off tick after the kill
        assertFalse((boolean) Reflect.get(panel, "ufoSoundOn"),
                "the warble stops with the saucer");

        // any other shot: one of the rolled values 50/100/150
        Reflect.set(panel, "ufoTimer", 0.0);
        Reflect.set(panel, "totalShots", 16);
        update(0.016);
        assertTrue(ufo.active, "a fresh saucer crosses back");
        bullets.add(Projectile.bullet(ufo.x, Constants.UFO_Y + 2));
        update(0.016);
        assertFalse(ufo.active, "and it goes down too");
        int earned = (int) Reflect.get(panel, "score") - 300;
        assertTrue(List.of(50, 100, 150).contains(earned), "rolled saucer payout: " + earned);
    }

    @Test
    void formationReachingTheCannonRowIsAnAutomaticLoss() {
        spawnPanel();
        start();

        // teleport the formation right onto the cannon row, then let it step
        AlienGrid grid = Reflect.getAs(panel, "grid");
        Reflect.set(grid, "ty", 210.0);
        update(0.2);

        assertEquals("PLAYER_DYING", state(), "the march reached the cannons");
        assertEquals(0, (int) Reflect.get(panel, "lives"), "the automatic loss costs the last life");
    }

    @Test
    void clearingTheGridStartsTheNextWave() {
        spawnPanel();
        start();

        List<Alien> aliens = Reflect.getAs(panel, "aliens");
        AlienGrid grid = Reflect.getAs(panel, "grid");
        for (Alien alien : aliens) {
            alien.kill();
            grid.kill(alien.row, alien.col);
        }

        update(0.016);
        assertEquals("WAVE_CLEAR", state(), "an empty formation clears the wave");

        Reflect.set(panel, "waveClearT", 0.0);
        update(0.016);
        assertEquals("PLAYING", state(), "after the pause the next wave reloads");
        assertWaveMessage("WAVE 2");
        assertEquals(55, grid.aliveCount(), "the formation is whole again");
        assertTrue(aliens.stream().allMatch(Alien::isAlive), "aliens respawned with the wave");
    }

    @Test
    void deathAnimationFlickersThenRespawnsOrEndsTheGame() {
        spawnPanel();
        start();

        // first death: respawn while lives remain
        Reflect.invoke(panel, "killPlayer");
        assertEquals("PLAYER_DYING", state());
        update(0.07); // one flicker frame of the crash animation
        Reflect.set(panel, "deathT", 0.0);
        update(0.016);
        assertEquals("PLAYING", state(), "back on the field with lives left");
        assertEquals(2, (int) Reflect.get(panel, "lives"));

        // last death: game over
        Reflect.set(panel, "lives", 0);
        Reflect.invoke(panel, "killPlayer");
        Reflect.set(panel, "deathT", 0.0);
        update(0.016);
        assertEquals("GAME_OVER", state(), "running out of lives ends the game");
        assertTrue(Reflect.<Group>getAs(panel, "gameOverOverlay").isVisible());
    }

    @Test
    void bombsBurstOnTheFloorAndCarveTheBunker() {
        spawnPanel();
        start();

        // the bomb-drop timer can spawn a squiggly bomb under a live column
        Reflect.set(panel, "bombTimer", 0.0);
        update(0.016);
        assertTrue(bombs.size() >= 1, "the spawner dropped a bomb");
        dropTrackedProjectiles();

        // a bomb falling into a bunker eats a crater top-down
        Projectile bunkerBomb = Projectile.bomb(29, Constants.SHIELD_Y - 2, 55);
        bombs.add(bunkerBomb);
        content.getChildren().add(bunkerBomb.node);
        update(0.016);
        assertFalse(bombs.contains(bunkerBomb), "the bunker bite removes the bomb");
        assertNull(bunkerBomb.node.getParent());

        // a bomb reaching the green floor line bursts
        Projectile floorBomb = Projectile.bomb(100, Constants.FLOOR_Y - 1, 55);
        bombs.add(floorBomb);
        content.getChildren().add(floorBomb.node);
        update(0.016);
        assertFalse(bombs.contains(floorBomb), "the floor burst removes the bomb");
        List<?> fxList = Reflect.getAs(panel, "fxList");
        assertEquals(1, fxList.size(), "one live burst effect remains");
    }

    @Test
    void risingPulseCarvesTheBunkerFromBelow() {
        spawnPanel();
        start();

        // pulse aimed into the bunker's left leg, rising bottom-up
        Projectile pulse = Projectile.bullet(Constants.SHIELD_LEFT0 + 2,
                Constants.SHIELD_Y + 13);
        bullets.add(pulse);
        content.getChildren().add(pulse.node);

        update(0.016);
        assertTrue(bullets.isEmpty(), "the bunker bite wastes the pulse");
        assertNull(pulse.node.getParent());
        List<?> fxList = Reflect.getAs(panel, "fxList");
        assertTrue(fxList.isEmpty(), "bunker bites leave no burst effect");
    }

    @Test
    void alienKillSpawnsABurstThatExpiresByTtl() {
        spawnPanel();
        start();

        Alien alien = firstAliveAlien();
        AlienGrid grid = Reflect.getAs(panel, "grid");
        double worldX = Constants.GRID_START_X + grid.getTx() + alien.localX();
        double worldY = Constants.GRID_TOP_Y + grid.getTy() + alien.localY();
        bullets.add(Projectile.bullet(worldX + alien.kind.width() / 2.0 - 0.5,
                worldY + alien.kind.height() / 2.0 - 2));

        update(0.016);
        assertFalse(alien.isAlive(), "the invader went down through the real loop");
        assertTrue((int) Reflect.get(panel, "score") > 0, "and paid out points");
        List<?> fxList = Reflect.getAs(panel, "fxList");
        assertEquals(1, fxList.size(), "the kill burst is on screen");

        update(1.0); // far beyond the burst's ttl
        assertTrue(fxList.isEmpty(), "expired burst effects detach and vanish");
    }

    // ------------------------------------------------------------ helpers

    private Alien firstAliveAlien() {
        List<Alien> aliens = Reflect.getAs(panel, "aliens");
        return aliens.stream().filter(Alien::isAlive).findFirst().orElseThrow();
    }
}
