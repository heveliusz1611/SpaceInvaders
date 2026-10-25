package com.spaceinvaders.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.spaceinvaders.testutil.FxToolkit;

/**
 * Headless checks of the keyboard state: a held key stays in the down set,
 * a fresh press lands in the one-shot buffer, and endFrame drains it.
 * Handlers are driven through the real Scene wiring so attach() is covered.
 * The scene is constructed and driven on the plain test thread, like the rest
 * of the view tests keep their scene graphs off the FX thread.
 */
class KeyInputTest {

    @BeforeAll
    static void bootFx() {
        // only boots the toolkit; this class never schedules onto the FX thread
        FxToolkit.ensure();
    }

    @Test
    void attachWiresBothHandlersOntoTheScene() {
        KeyInput input = new KeyInput();
        Scene scene = new Scene(new Group());
        input.attach(scene);

        // the scene now routes its key events straight into this instance
        EventHandler<KeyEvent> keyPressed = pressHandler(scene);
        EventHandler<KeyEvent> keyReleased = releaseHandler(scene);
        keyPressed.handle(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertTrue(input.held(KeyCode.ENTER), "the wired handler feeds the down set");
        keyReleased.handle(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.ENTER));
        assertFalse(input.held(KeyCode.ENTER), "and the release handler drains it");
    }

    @Test
    void keyPressedThenReleasedMovesThroughBothSets() {
        KeyInput input = new KeyInput();
        Scene scene = new Scene(new Group());
        input.attach(scene);
        EventHandler<KeyEvent> press = pressHandler(scene);
        EventHandler<KeyEvent> release = releaseHandler(scene);

        assertFalse(input.held(KeyCode.ENTER), "nothing down yet");
        assertFalse(input.pressed(KeyCode.ENTER), "nothing pressed yet");

        press.handle(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertTrue(input.held(KeyCode.ENTER), "the key is down");
        assertTrue(input.pressed(KeyCode.ENTER), "fresh press is buffered");

        // repeat press while held: no second one-shot press (down set short-circuits)
        press.handle(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.ENTER));
        assertTrue(input.pressed(KeyCode.ENTER));

        release.handle(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.ENTER));
        assertFalse(input.held(KeyCode.ENTER), "released key left the down set");
        assertTrue(input.pressed(KeyCode.ENTER), "the buffer survives until the frame ends");

        input.endFrame();
        assertFalse(input.pressed(KeyCode.ENTER), "endFrame drains the one-shot buffer");
    }

    @Test
    void repressingAReleasedKeyIsAFreshOneShot() {
        KeyInput input = new KeyInput();
        Scene scene = new Scene(new Group());
        input.attach(scene);
        EventHandler<KeyEvent> press = pressHandler(scene);
        EventHandler<KeyEvent> release = releaseHandler(scene);

        press.handle(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        release.handle(keyEvent(KeyEvent.KEY_RELEASED, KeyCode.LEFT));
        input.endFrame();

        press.handle(keyEvent(KeyEvent.KEY_PRESSED, KeyCode.LEFT));
        assertTrue(input.pressed(KeyCode.LEFT), "a re-press after release is a fresh one-shot");
    }

    private static KeyEvent keyEvent(javafx.event.EventType<KeyEvent> type, KeyCode code) {
        return new KeyEvent(type, "", "", code, false, false, false, false);
    }

    private static EventHandler<KeyEvent> pressHandler(Scene scene) {
        return retype(scene.getOnKeyPressed());
    }

    private static EventHandler<KeyEvent> releaseHandler(Scene scene) {
        return retype(scene.getOnKeyReleased());
    }

    /** The scene stores handlers as EventHandler&lt;? super KeyEvent&gt; - re-typed for driving. */
    @SuppressWarnings("unchecked")
    private static EventHandler<KeyEvent> retype(Object handler) {
        return (EventHandler<KeyEvent>) handler;
    }
}
