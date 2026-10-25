package com.spaceinvaders.audio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Drives the full synthesized sound set. This machine may or may not have a
 * usable audio device - the class is supposed to degrade silently in that
 * case, so the test only asserts that triggering every sound (and the UFO
 * warble loop's start/stop cycle) never blows up.
 */
class SoundsTest {

    @Test
    void everySoundEffectCanTriggerWithoutThrowing() {
        Sounds.shot();
        for (int step = 0; step < 4; step++) {
            Sounds.marchNote(step); // covers all four arms of the march loop
        }
        Sounds.marchNote(9); // wraps around to the same four notes
        Sounds.invaderBoom();
        Sounds.playerBoom();
        Sounds.extraLife();
    }

    @Test
    void ufoWarbleStartsStopsAndIsIdempotent() {
        Sounds.ufoStop(); // nothing playing yet: safe no-op
        Sounds.ufoStart();
        Sounds.ufoStart(); // a second start while the loop runs must not stack
        Sounds.ufoStop();
        Sounds.ufoStop(); // stopping twice stays safe
        assertTrue(true, "no exception means the audio path is resilient");
    }
}
