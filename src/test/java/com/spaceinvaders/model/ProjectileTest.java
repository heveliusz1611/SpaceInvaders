package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.junit.jupiter.api.Test;

/** Headless checks of the player pulse and the alien bomb: motion, size, pose. */
class ProjectileTest {

    @Test
    void bulletIsAOneByFourWhitePulse() {
        Projectile bullet = Projectile.bullet(0, 0);
        assertEquals(Projectile.Kind.BULLET, bullet.kind);
        assertEquals(1, bullet.w);
        assertEquals(4, bullet.h);

        Group sprite = bullet.node;
        int pixels = 0;
        for (Node pixel : sprite.getChildren()) {
            Rectangle rect = (Rectangle) pixel;
            assertEquals(1, rect.getWidth());
            assertEquals(1, rect.getHeight());
            assertEquals(Constants.HUD_WHITE, rect.getFill());
            assertEquals(0, rect.getX());
            pixels++;
        }
        assertEquals(4, pixels, "bullet must be 4 stacked pixels");
    }

    @Test
    void bulletFliesUpwardAtBulletSpeed() {
        Projectile bullet = Projectile.bullet(100, 100);
        bullet.update(0.5);
        assertEquals(100 - Constants.BULLET_SPEED * 0.5, bullet.y, 1e-9);
        assertEquals(100, bullet.x, "bullets never drift sideways");
    }

    @Test
    void bombFallsDownwardAtItsOwnSpeed() {
        Projectile bomb = Projectile.bomb(100, 40, 55);
        assertEquals(Projectile.Kind.BOMB, bomb.kind);
        assertEquals(3, bomb.w);
        assertEquals(7, bomb.h);
        bomb.update(0.5);
        assertEquals(40 + 55 * 0.5, bomb.y, 1e-9);
        assertEquals(100, bomb.x, "bombs never drift sideways");
    }

    @Test
    void bombHasTwoAlternatingSquiggleFrames() {
        Projectile bomb = Projectile.bomb(0, 0, 55);
        Group sprite = bomb.node;
        assertEquals(2, sprite.getChildren().size(), "bomb needs exactly two anim frames");

        Group frameA = (Group) sprite.getChildren().get(0);
        Group frameB = (Group) sprite.getChildren().get(1);
        assertTrue(frameA.isVisible(), "squiggle starts on frame A");
        assertFalse(frameB.isVisible(), "squiggle starts with frame B hidden");

        // nine-slice in a pattern count check: every frame is a 3x7 bitmap
        assertEquals(0, (int) Math.round(frameA.getChildren().stream()
                .mapToDouble(n -> ((Rectangle) n).getX()).min().orElse(-1)));
        assertEquals(2, (int) Math.round(frameA.getChildren().stream()
                .mapToDouble(n -> ((Rectangle) n).getX()).max().orElse(-1)));
        assertEquals(0, (int) Math.round(frameA.getChildren().stream()
                .mapToDouble(n -> ((Rectangle) n).getY()).min().orElse(-1)));
        assertEquals(6, (int) Math.round(frameA.getChildren().stream()
                .mapToDouble(n -> ((Rectangle) n).getY()).max().orElse(-1)));
        assertEquals(Constants.HUD_WHITE, ((Rectangle) frameA.getChildren().get(0)).getFill());
    }

    @Test
    void bombAnimFlipsEveryTenthOfASecond() {
        Projectile bomb = Projectile.bomb(0, 0, 55);
        Group frameA = (Group) bomb.node.getChildren().get(0);
        Group frameB = (Group) bomb.node.getChildren().get(1);

        bomb.update(0.05);
        assertTrue(frameA.isVisible(), "under the interval the frame must not flip yet");
        bomb.update(0.05);
        assertFalse(frameA.isVisible(), "crossing 0.1s flips exactly once");
        assertTrue(frameB.isVisible());
        bomb.update(0.05);
        assertFalse(frameA.isVisible(), "only full 0.1s slices flip");
        bomb.update(0.05);
        assertTrue(frameA.isVisible(), "back to frame A after 0.2s");
    }

    @Test
    void renderSnapsToWholeGamePixels() {
        Projectile bullet = Projectile.bullet(10.4, 20.6);
        bullet.render();
        assertEquals(10, bullet.node.getTranslateX());
        assertEquals(21, bullet.node.getTranslateY());
    }

    @Test
    void bulletNodeIsFreshForEachShot() {
        // two bullets must never share a sprite, or removing one breaks the other
        Projectile a = Projectile.bullet(0, 0);
        Projectile b = Projectile.bullet(0, 0);
        assertTrue(a.node != b.node, "each bullet owns its sprite");
        assertTrue(a.node.getParent() == null, "shots start detached from the scene");
    }

    @Test
    void factorySpritesForBothKindsExist() {
        assertEquals(4, SpriteFactory.bullet().getChildren().size());
        Group[] frames = SpriteFactory.bombFrames();
        assertEquals(2, frames.length);
        assertTrue(frames[0] != frames[1], "frames must be distinct nodes");
        assertEquals(Color.web("#F8F8F8"), ((Rectangle) frames[1].getChildren().get(0)).getFill());
    }
}
