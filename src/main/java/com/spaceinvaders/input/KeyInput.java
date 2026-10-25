package com.spaceinvaders.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Keyboard input: a set of currently held keys plus a per-frame "pressed"
 * buffer for one-shot actions (start, pause). Cleared after every update.
 */
public final class KeyInput {
    private final Set<KeyCode> down = EnumSet.noneOf(KeyCode.class);
    private final Set<KeyCode> pressed = EnumSet.noneOf(KeyCode.class);

    public void attach(Scene scene) {
        scene.setOnKeyPressed(this::onPress);
        scene.setOnKeyReleased(this::onRelease);
    }

    private void onPress(KeyEvent event) {
        if (!down.contains(event.getCode())) {
            pressed.add(event.getCode());
        }
        down.add(event.getCode());
    }

    private void onRelease(KeyEvent event) {
        down.remove(event.getCode());
    }

    public boolean held(KeyCode key) {
        return down.contains(key);
    }

    /** One-shot: true only during the update frame in which the key went down. */
    public boolean pressed(KeyCode key) {
        return pressed.contains(key);
    }

    public void endFrame() {
        pressed.clear();
    }
}
