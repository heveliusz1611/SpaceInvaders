package com.spaceinvaders.sprites;

import com.spaceinvaders.model.AlienKind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Sanity checks of the retro sprite bitmaps. */
class SpriteDataTest {

    private void assertRectangularRows(String[] pattern, String name) {
        int width = SpriteData.width(pattern);
        assertTrue(width > 0, name + " must not be empty");
        for (String row : pattern) {
            assertEquals(width, row.length(), name + " rows must share one width");
        }
    }

    @Test
    void invaderFramesMatchDimensions() {
        for (AlienKind kind : AlienKind.values()) {
            String[] a = kind.frame(0);
            String[] b = kind.frame(1);
            assertRectangularRows(a, kind.name() + " A");
            assertRectangularRows(b, kind.name() + " B");
            assertEquals(SpriteData.height(a), SpriteData.height(b), kind + " frame heights");
            assertEquals(SpriteData.width(a), SpriteData.width(b), kind + " frame widths");
            assertEquals(kind.width(), SpriteData.width(a));
            assertEquals(kind.height(), SpriteData.height(a));
        }
    }

    @Test
    void formationRowsUseOriginalTypes() {
        assertEquals(AlienKind.SQUID, AlienKind.forRow(0));
        assertEquals(AlienKind.CRAB, AlienKind.forRow(1));
        assertEquals(AlienKind.CRAB, AlienKind.forRow(2));
        assertEquals(AlienKind.OCTOPUS, AlienKind.forRow(3));
        assertEquals(AlienKind.OCTOPUS, AlienKind.forRow(4));
    }

    @Test
    void fixedPointValuesMatchArcade() {
        assertEquals(30, AlienKind.SQUID.points);
        assertEquals(20, AlienKind.CRAB.points);
        assertEquals(10, AlienKind.OCTOPUS.points);
    }

    @Test
    void otherSpritesAreWellFormed() {
        assertRectangularRows(SpriteData.CANNON, "CANNON");
        assertRectangularRows(SpriteData.UFO, "UFO");
        assertRectangularRows(SpriteData.EXPLOSION, "EXPLOSION");
        assertRectangularRows(SpriteData.CANNON_DEATH_A, "CANNON_DEATH_A");
        assertRectangularRows(SpriteData.CANNON_DEATH_B, "CANNON_DEATH_B");
        assertRectangularRows(SpriteData.BOMB_A, "BOMB_A");
        assertRectangularRows(SpriteData.BOMB_B, "BOMB_B");
        assertRectangularRows(SpriteData.SHIELD, "SHIELD");
    }
}
