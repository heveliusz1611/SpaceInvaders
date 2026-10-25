package com.spaceinvaders.view;

import com.spaceinvaders.testutil.FxToolkit;
import com.spaceinvaders.testutil.Reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.text.Text;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Headless checks of the HUD build-up: label/value layout, live readouts and
 * reserve-cannon icon counts.
 */
class HudTest {

    private Hud hud;

    @BeforeAll
    static void bootFx() {
        FxToolkit.ensure();
    }

    private Text text(String field) {
        return Reflect.getAs(hud, field);
    }

    @Test
    void scoreAndHiScoreDigitsSitUnderTheirLabels() {
        hud = new Hud();
        Text score = text("scoreValue");
        Text hi = text("hiValue");
        // the digits must sit on the line below each label; if a value Text
        // never gets positioned it piles up in the top-left corner and the
        // HI-SCORE readout goes permanently blind
        assertEquals(8, score.getX(), "SCORE digits align with the SCORE<1> label");
        assertEquals(17, score.getY(), "digits on the line below the label");
        assertEquals(152, hi.getX(), "HI-SCORE digits align with the HI-SCORE label");
        assertEquals(17, hi.getY(), "digits on the line below the label");
        assertTrue(score.isVisible() && hi.isVisible());
        assertTrue(text("scoreValue").getFont().getSize() == 8,
                "readouts use the fixed 8px monospaced face");
    }

    @Test
    void constructorShowsEmptyScoreAndTwoReserveIcons() {
        hud = new Hud();
        assertEquals("00000", text("scoreValue").getText(), "panel opens with a blank score");
        assertEquals("00000", text("hiValue").getText(), "panel opens with a blank hi-score");
        assertVisibleIcons(2, 3); // initial lives 3 -> two reserves
    }

    @Test
    void updateWritesAllReadouts() {
        hud = new Hud();
        hud.update(12, 34, 2, 4);
        assertEquals("00012", text("scoreValue").getText(), "score is zero-padded to five");
        assertEquals("00034", text("hiValue").getText(), "hi-score is zero-padded to five");
        assertEquals("WAVE 02", text("waveValue").getText());
        assertEquals("4", text("livesValue").getText());
    }

    @Test
    void reserveIconsFollowTheLives() {
        hud = new Hud();
        assertVisibleIcons(0, 1); // only the active cannon, no reserves
        assertVisibleIcons(1, 2);
        assertVisibleIcons(2, 3);
        assertVisibleIcons(3, 4);
    }

    private void assertVisibleIcons(int expected, int lives) {
        hud.update(0, 0, 1, lives);
        List<?> icons = Reflect.getAs(hud, "lifeIcons");
        assertEquals(3, icons.size(), "exactly three reserve cannon icons are built");
        long shown = icons.stream().filter(i -> ((javafx.scene.Node) i).isVisible()).count();
        assertEquals(expected, shown, () -> "visible reserves with " + lives + " lives");
    }
}
