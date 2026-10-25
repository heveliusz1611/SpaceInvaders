package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.scene.Group;

/** The player's green cannon. Fixed speed, clamped to the playfield. */
public final class Player {
    public static final int W = 13;
    public static final int H = 8;

    public final Group node = SpriteFactory.player();
    public double x;

    public Player() {
        respawn();
        node.setTranslateY(Constants.PLAYER_Y);
    }

    /** Moves by the given delta (game pixels), clamped to screen margins. */
    public void move(double dx) {
        double min = 4;
        double max = Constants.GAME_W - W - 4;
        x = Math.max(min, Math.min(max, x + dx));
        node.setTranslateX(Math.round(x));
    }

    /** Resets the cannon to its center spawn position. */
    public void respawn() {
        x = (Constants.GAME_W - W) / 2.0;
        node.setTranslateX(Math.round(x));
    }

    public double centerX() {
        return x + W / 2.0;
    }
}
