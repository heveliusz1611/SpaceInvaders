package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.scene.Group;

/**
 * A single invader in the formation. Its graphic is a Group holding both
 * animation frames, so the march animation only toggles visibility.
 */
public final class Alien {
    public final int row;
    public final int col;
    public final AlienKind kind;
    public final Group node;

    private final Group frameA;
    private final Group frameB;
    private boolean alive = true;
    private boolean showA = true;

    public Alien(int row, int col) {
        this.row = row;
        this.col = col;
        this.kind = AlienKind.forRow(row);
        this.frameA = SpriteFactory.alien(kind, false);
        this.frameB = SpriteFactory.alien(kind, true);
        this.frameB.setVisible(false);
        this.node = new Group(frameA, frameB);
        this.node.setTranslateX(col * Constants.CELL_W + (Constants.CELL_W - kind.width()) / 2.0);
        this.node.setTranslateY(row * Constants.CELL_H);
    }

    /** Flips to the next march frame (called once per formation step). */
    public void toggleFrame() {
        showA = !showA;
        frameA.setVisible(showA);
        frameB.setVisible(!showA);
    }

    public void kill() {
        alive = false;
        node.setVisible(false);
    }

    /** Back to a live invader with its first march frame (wave reload). */
    public void reset() {
        alive = true;
        node.setVisible(true);
        showA = true;
        frameA.setVisible(true);
        frameB.setVisible(false);
    }

    public boolean isAlive() {
        return alive;
    }

    /** X of this invader inside the formation group (game pixels). */
    public double localX() {
        return node.getTranslateX();
    }

    /** Y of this invader inside the formation group (game pixels). */
    public double localY() {
        return node.getTranslateY();
    }
}
