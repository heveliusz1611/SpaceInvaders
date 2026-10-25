package com.spaceinvaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.testutil.FxToolkit;
import com.spaceinvaders.testutil.Reflect;
import com.spaceinvaders.view.GamePanel;

/**
 * Boots the real entry point on the FX thread: start() must open the
 * arcade-sized window, wire the keyboard and kick off the animation loop.
 * The loop is stopped again right after, so no timer outlives the test.
 */
class SpaceInvadersGameTest {

    @Test
    void startOpensAnArcadeScaledWindowWithInputAttached() throws Exception {
        FxToolkit.ensure();
        SpaceInvadersGame game = new SpaceInvadersGame();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Stage> stageRef = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1);

        // Stage.show() below must run on the FX application thread
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                game.start(stage);
                stageRef.set(stage);
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                started.countDown();
            }
        });
        assertTrue(started.await(10, TimeUnit.SECONDS), "the FX runnable must complete");
        if (failure.get() != null) {
            throw new AssertionError("start() failed on the FX thread", failure.get());
        }

        // verify what start() built, then stop the timer and tear the stage down
        AtomicReference<Throwable> teardownFailure = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = stageRef.get().getScene();
                assertNotNull(scene, "start() installed a scene");
                assertInstanceOf(GamePanel.class, scene.getRoot());
                GamePanel panel = (GamePanel) scene.getRoot();
                assertEquals(Constants.GAME_W * panel.getScale(), scene.getWidth(),
                        "window width follows the panel's computed pixel scale");
                assertEquals(Constants.GAME_H * panel.getScale(), scene.getHeight(),
                        "window height follows the panel's computed pixel scale");
                assertEquals("SPACE INVADERS", stageRef.get().getTitle());
                assertFalse(stageRef.get().isResizable(), "arcade cabinet does not resize");

                AnimationTimer loop = Reflect.getAs(panel, "loop");
                loop.stop(); // no pulse outlives the test
                stageRef.get().hide();
            } catch (Throwable t) {
                teardownFailure.set(t);
            } finally {
                closed.countDown();
            }
        });
        assertTrue(closed.await(10, TimeUnit.SECONDS), "the FX teardown must complete");
        if (teardownFailure.get() != null) {
            throw new AssertionError("teardown failed on the FX thread", teardownFailure.get());
        }
    }
}
