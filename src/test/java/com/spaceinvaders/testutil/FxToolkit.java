package com.spaceinvaders.testutil;

import javafx.application.Platform;

/**
 * Boots the JavaFX toolkit once per test JVM so that view-layer code that
 * needs fonts/text (Hud, GamePanel overlays) can run headless in tests.
 * The toolkit stays up for the whole run: subsequent calls are no-ops.
 */
public final class FxToolkit {

    private FxToolkit() {}

    public static void ensure() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException alreadyRunning) {
            // toolkit booted by an earlier test class - fine
        }
    }
}
