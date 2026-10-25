package com.spaceinvaders.model;

import com.spaceinvaders.sprites.SpriteData;

import javafx.scene.paint.Color;

/**
 * The three original invader types. Each one carries its fixed arcade point
 * value, its retro color and its two animation frames - all constant for the
 * whole game (one squid row, two crab rows, two octopus rows).
 */
public enum AlienKind {
    SQUID(30, Color.web("#FF6FE0"), SpriteData.SQUID_A, SpriteData.SQUID_B),
    CRAB(20, Color.web("#7AE7FF"), SpriteData.CRAB_A, SpriteData.CRAB_B),
    OCTOPUS(10, Color.web("#4FF05A"), SpriteData.OCTOPUS_A, SpriteData.OCTOPUS_B);

    public final int points;
    public final Color color;
    private final String[] frameA;
    private final String[] frameB;
    private final int width;
    private final int height;

    AlienKind(int points, Color color, String[] frameA, String[] frameB) {
        this.points = points;
        this.color = color;
        this.frameA = frameA;
        this.frameB = frameB;
        this.width = Math.max(SpriteData.width(frameA), SpriteData.width(frameB));
        this.height = SpriteData.height(frameA);
    }

    /** Which type marches in the given formation row (row 0 is the top row). */
    public static AlienKind forRow(int row) {
        return switch (row) {
            case 0 -> SQUID;
            case 1, 2 -> CRAB;
            default -> OCTOPUS;
        };
    }

    public String[] frame(int index) {
        return index == 0 ? frameA : frameB;
    }

    public int width() { return width; }
    public int height() { return height; }
}
