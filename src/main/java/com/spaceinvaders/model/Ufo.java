package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.scene.Group;

/** The mystery saucer that crosses the top row for bonus points. */
public final class Ufo {
    public static final int W = 16;
    public static final int H = 7;

    public final Group node = SpriteFactory.ufo();
    public double x;
    public int dir = 1;
    public boolean active;

    public Ufo() {
        node.setVisible(false);
    }

    /** Launches the saucer from a screen edge in the given direction (-1/+1). */
    public void spawn(int direction) {
        dir = direction;
        x = direction > 0 ? -W : Constants.GAME_W;
        active = true;
        node.setVisible(true);
        render();
    }

    public void update(double dt) {
        x += dir * Constants.UFO_SPEED * dt;
        if ((dir > 0 && x > Constants.GAME_W) || (dir < 0 && x + W < 0)) {
            despawn();
        } else {
            render();
        }
    }

    public void despawn() {
        active = false;
        node.setVisible(false);
    }

    public void render() {
        node.setTranslateX(Math.round(x));
        node.setTranslateY(Constants.UFO_Y);
    }
}
