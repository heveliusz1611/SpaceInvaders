package com.spaceinvaders.sprites;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.model.AlienKind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import org.junit.jupiter.api.Test;

/** Headless checks of SpriteFactory: pattern -> one rectangle per 'X'. */
class SpriteFactoryTest {

    private static int expectedPixels(String[] pattern) {
        int count = 0;
        for (String row : pattern) {
            for (int i = 0; i < row.length(); i++) {
                if (row.charAt(i) == 'X') {
                    count++;
                }
            }
        }
        return count;
    }

    private void assertPixelsMatchPattern(String[] pattern, Color color, Group sprite) {
        assertEquals(expectedPixels(pattern), sprite.getChildren().size(),
                "one rectangle per opaque bitmap pixel");
        for (Node node : sprite.getChildren()) {
            Rectangle rect = (Rectangle) node;
            assertEquals(1, rect.getWidth(), "pixels are 1x1 game units");
            assertEquals(1, rect.getHeight());
            assertEquals(color, rect.getFill());
            int col = (int) rect.getX();
            int row = (int) rect.getY();
            assertTrue(row >= 0 && row < pattern.length, "pixel row inside bitmap");
            assertTrue(col >= 0 && col < pattern[row].length(), "pixel col inside bitmap");
            assertEquals('X', pattern[row].charAt(col), "only drawn where the bitmap is set");
        }
    }

    @Test
    void pixelsDrawExactlyWhereThePatternIsSet() {
        assertPixelsMatchPattern(new String[] {"XX.", "..X"}, Color.RED,
                SpriteFactory.pixels(new String[] {"XX.", "..X"}, Color.RED));
        assertEquals(0, SpriteFactory.pixels(new String[] {"."}, Color.RED).getChildren().size(),
                "no pixels for an empty bitmap");
    }

    @Test
    void cannonIsThirteenWideAndGreen() {
        assertPixelsMatchPattern(SpriteData.CANNON, Constants.PLAYER_GREEN, SpriteFactory.player());
    }

    @Test
    void ufoSpriteUsesSaucerRed() {
        assertPixelsMatchPattern(SpriteData.UFO, Constants.UFO_RED, SpriteFactory.ufo());
    }

    @Test
    void explosionTakesAnyColor() {
        assertPixelsMatchPattern(SpriteData.EXPLOSION, Constants.HUD_WHITE,
                SpriteFactory.explosion(Constants.HUD_WHITE));
        assertPixelsMatchPattern(SpriteData.EXPLOSION, AlienKind.OCTOPUS.color,
                SpriteFactory.explosion(AlienKind.OCTOPUS.color));
    }

    @Test
    void deathFramesAreTwoDistinctGroups() {
        Group[] frames = SpriteFactory.playerDeathFrames();
        assertEquals(2, frames.length);
        assertNotSame(frames[0], frames[1], "the two debris frames must be distinct");
        assertPixelsMatchPattern(SpriteData.CANNON_DEATH_A, Constants.PLAYER_GREEN, frames[0]);
        assertPixelsMatchPattern(SpriteData.CANNON_DEATH_B, Constants.PLAYER_GREEN, frames[1]);
    }

    @Test
    void bombFramesAreTwoDistinctGroups() {
        Group[] frames = SpriteFactory.bombFrames();
        assertEquals(2, frames.length);
        assertNotSame(frames[0], frames[1], "the two squiggle frames must be distinct");
        assertPixelsMatchPattern(SpriteData.BOMB_A, Constants.HUD_WHITE, frames[0]);
        assertPixelsMatchPattern(SpriteData.BOMB_B, Constants.HUD_WHITE, frames[1]);
    }

    @Test
    void alienSpritesMatchTheirKindFrames() {
        for (AlienKind kind : AlienKind.values()) {
            assertPixelsMatchPattern(kind.frame(0), kind.color, SpriteFactory.alien(kind, false));
            assertPixelsMatchPattern(kind.frame(1), kind.color, SpriteFactory.alien(kind, true));
        }
    }
}
