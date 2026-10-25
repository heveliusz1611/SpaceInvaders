package com.spaceinvaders.sprites;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.model.AlienKind;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Builds sprite graphics as pure JavaFX node trees: every opaque bitmap pixel
 * becomes a 1x1 {@link Rectangle} grouped into a {@link Group}. Sprites are
 * laid out in game coordinates and the whole playfield is scaled uniformly.
 * No image files are used anywhere.
 */
public final class SpriteFactory {
    private SpriteFactory() {}

    /** Renders a pixel pattern as a Group of 1x1 rectangles. */
    public static Group pixels(String[] pattern, Color color) {
        Group sprite = new Group();
        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == 'X') {
                    Rectangle pixel = new Rectangle(col, row, 1, 1);
                    pixel.setFill(color);
                    sprite.getChildren().add(pixel);
                }
            }
        }
        return sprite;
    }

    /** One animation frame of an invader. */
    public static Group alien(AlienKind kind, boolean secondFrame) {
        return pixels(kind.frame(secondFrame ? 1 : 0), kind.color);
    }

    public static Group player() {
        return pixels(SpriteData.CANNON, Constants.PLAYER_GREEN);
    }

    public static Group ufo() {
        return pixels(SpriteData.UFO, Constants.UFO_RED);
    }

    public static Group explosion(Color color) {
        return pixels(SpriteData.EXPLOSION, color);
    }

    /** The two alternating debris frames shown when the cannon is destroyed. */
    public static Group[] playerDeathFrames() {
        return new Group[] {
                pixels(SpriteData.CANNON_DEATH_A, Constants.PLAYER_GREEN),
                pixels(SpriteData.CANNON_DEATH_B, Constants.PLAYER_GREEN)
        };
    }

    /** Single player-bullet sprite (1x4 white pulse). */
    public static Group bullet() {
        return pixels(new String[] {"X", "X", "X", "X"}, Constants.HUD_WHITE);
    }

    /** The two alternating frames of a squiggly alien bomb. */
    public static Group[] bombFrames() {
        return new Group[] {
                pixels(SpriteData.BOMB_A, Constants.HUD_WHITE),
                pixels(SpriteData.BOMB_B, Constants.HUD_WHITE)
        };
    }
}
