package com.spaceinvaders.model;

import com.spaceinvaders.sprites.SpriteData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Headless checks of bunker solidity, hit-scan and blast carving. */
class ShieldTest {

    @Test
    void patternDimensions() {
        assertEquals(Shield.W, 22);
        assertEquals(Shield.H, 16);
        assertEquals(Shield.H, SpriteData.SHIELD.length);
        for (String row : SpriteData.SHIELD) {
            assertEquals(Shield.W, row.length(), "every shield row must be 22 wide");
        }
    }

    @Test
    void bombFromAboveHitsTopmostSolidCell() {
        Shield shield = new Shield(0, 0);
        // Bomb (3 wide) descending straight onto the middle of the bunker
        int[] first = shield.hit(10.0, 0.0, 3.0, 7.0, false);
        assertNotNull(first);
        assertEquals(0, first[0], "falling bomb first bites the highest solid row");
        shield.bombImpact(first[0], first[1]);
        int[] after = shield.hit(10.0, 0.0, 3.0, 7.0, false);
        if (after != null) {
            // ragged craters may leave stragglers, but always deeper in
            assertTrue(after[0] > first[0], "post-blast hit must be deeper than before");
        }
    }

    @Test
    void bulletFromBelowHitsBottommostSolidCell() {
        Shield shield = new Shield(0, 0);
        // 1x2 shottip at a solid wall column (col 3), just entering the bottom
        int[] cell = shield.hit(3.0, 14.0, 1.0, 2.0, true);
        assertNotNull(cell);
        assertEquals(15, cell[0], "rising shot first bites the lowest solid row");
        shield.bulletImpact(cell[0], cell[1]);
        assertNull(shield.hit(3.0, 14.0, 1.0, 2.0, true),
                "the shot's column band is fully carved by the blast");
    }

    @Test
    void missReturnsNull() {
        Shield shield = new Shield(0, 0);
        // Left of the bunker entirely (negative x maps outside, clamps skip it)
        int[] cell = shield.hit(40.0, 5.0, 2.0, 2.0, true);
        assertNull(cell);
    }

    @Test
    void blastClearsRadiusAroundImpact() {
        Shield shield = new Shield(0, 0);
        shield.blast(5, 10, 3, 0);
        // center and well inside the disc must be gone
        assertNull(shield.hit(10.0, 5.0, 1.0, 1.0, true),
                "center and immediately around the blast must be carved away");
        // far pixels (solid corner of the pattern, far from the blast) survive
        int[] far = shield.hit(4.0, 4.0, 1.0, 1.0, true);
        assertNotNull(far, "pixels far from the blast survive");
    }

    @Test
    void blastCannotLeaveBounds() {
        Shield shield = new Shield(0, 0);
        shield.blast(0, 0, 4, 8);
        shield.blast(Shield.H - 1, Shield.W - 1, 4, 8);
        // simply must not throw; bunker still reports hits elsewhere
        assertNotNull(shield.hit(15.0, 7.0, 2.0, 2.0, true));
    }
}
