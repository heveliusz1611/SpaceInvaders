package com.spaceinvaders.view;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.model.Alien;
import com.spaceinvaders.model.AlienGrid;
import com.spaceinvaders.model.Player;
import com.spaceinvaders.model.Projectile;
import com.spaceinvaders.testutil.FxToolkit;
import com.spaceinvaders.testutil.Reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Headless checks of GamePanel's projectile lifecycle: every projectile that
 * leaves the tracking lists must also leave the scene graph, otherwise its
 * sprite stays frozen on the playfield as a leftover artifact.
 */
class GamePanelTest {

    private GamePanel panel;
    private Group content;
    private List<Projectile> bullets;
    private List<Projectile> bombs;

    @BeforeAll
    static void bootFx() {
        FxToolkit.ensure();
    }

    private void spawnPanel() {
        panel = new GamePanel();
        content = Reflect.getAs(panel, "content");
        bullets = Reflect.getAs(panel, "bullets");
        bombs = Reflect.getAs(panel, "bombs");
    }

    @Test
    void buildsThePlayfieldAndStartsIdleOnTheTitleScreen() {
        spawnPanel();
        assertFalse(content.getChildren().isEmpty(), "playfield has content");
        Hud hud = Reflect.getAs(panel, "hud");
        assertTrue(content.getChildren().contains(hud.node), "HUD is part of the scene");
        Player player = Reflect.getAs(panel, "player");
        assertTrue(content.getChildren().contains(player.node), "cannon is part of the scene");
        assertEquals("TITLE", String.valueOf(Reflect.get(panel, "state")),
                "opens on the title screen, not mid-game");
    }

    @Test
    void bulletThatDestroysAnInvaderLeavesNoSpriteBehind() {
        spawnPanel();
        Alien alien = firstAliveAlien();
        Projectile bullet = onScene(newAlienCenterBullet(alien));

        Reflect.invoke(panel, "checkBulletCollisions");

        assertTrue(bullets.isEmpty(), "the hit bullet left the tracking list");
        assertFalse(content.getChildren().contains(bullet.node),
                "the hit bullet's sprite must leave the scene graph - it used to linger here");
        assertNull(bullet.node.getParent(), "sprite fully detached");
        assertFalse(alien.isAlive(), "the invader went down");
        assertTrue((int) Reflect.get(panel, "score") > 0, "and paid out points");
    }

    @Test
    void bulletFlyingOffTheTopIsCleanedUp() {
        spawnPanel();
        Projectile bullet = Projectile.bullet(100, Constants.UFO_Y - 8);
        bullets.add(bullet);
        content.getChildren().add(bullet.node);

        Reflect.invoke(panel, "checkBulletCollisions");

        assertTrue(bullets.isEmpty());
        assertNull(bullet.node.getParent());
    }

    @Test
    void bombReachingTheFloorIsCleanedUp() {
        spawnPanel();
        Projectile bomb = Projectile.bomb(100, Constants.FLOOR_Y, 55);
        bombs.add(bomb);
        content.getChildren().add(bomb.node);

        Reflect.invoke(panel, "checkBombCollisions");

        assertTrue(bombs.isEmpty());
        assertNull(bomb.node.getParent(), "bombs must not linger at the floor line");
    }

    @Test
    void clearProjectilesDetachesEveryProjectileNode() {
        spawnPanel();
        Projectile bullet = Projectile.bullet(50, 100);
        Projectile bomb = Projectile.bomb(60, 110, 55);
        bullets.add(bullet);
        bombs.add(bomb);
        content.getChildren().addAll(bullet.node, bomb.node);

        Reflect.invoke(panel, "clearProjectiles");

        assertTrue(bullets.isEmpty() && bombs.isEmpty(), "lists emptied");
        assertTrue(bullet.node.getParent() == null && bomb.node.getParent() == null,
                "every sprite detached, ready for respawn/game-over/wave reload");
    }

    // ------------------------------------------------------------ helpers

    private Alien firstAliveAlien() {
        List<Alien> aliens = Reflect.getAs(panel, "aliens");
        return aliens.stream().filter(Alien::isAlive).findFirst().orElseThrow();
    }

    private Projectile onScene(Projectile shot) {
        bullets.add(shot);
        content.getChildren().add(shot.node);
        return shot;
    }

    /** A player pulse aimed at the exact center of the given invader. */
    private Projectile newAlienCenterBullet(Alien alien) {
        AlienGrid grid = Reflect.getAs(panel, "grid");
        double worldX = Constants.GRID_START_X + grid.getTx() + alien.localX();
        double worldY = Constants.GRID_TOP_Y + grid.getTy() + alien.localY();
        return Projectile.bullet(worldX + alien.kind.width() / 2.0 - 0.5,
                worldY + alien.kind.height() / 2.0 - 2);
    }
}
