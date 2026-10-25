package com.spaceinvaders.model;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.scene.Group;

/**
 * A flying shot: either the player's 1x4 pulse or a 3x7 squiggly alien bomb.
 * Movement is accumulated in game pixels per second; positions snap to whole
 * pixels when rendered so sprites stay crisp.
 */
public final class Projectile {
    public enum Kind { BULLET, BOMB }

    public final Kind kind;
    public final double speed;
    public final Group node;
    public final double w;
    public final double h;
    public double x;
    public double y;

    private final Group frameA;
    private final Group frameB;
    private double animClock;

    private Projectile(Kind kind, double speed, Group node, double w, double h,
                       Group frameA, Group frameB) {
        this.kind = kind;
        this.speed = speed;
        this.node = node;
        this.w = w;
        this.h = h;
        this.frameA = frameA;
        this.frameB = frameB;
    }

    public static Projectile bullet(double x, double y) {
        Projectile shot = new Projectile(Kind.BULLET, Constants.BULLET_SPEED,
                SpriteFactory.bullet(), 1, 4, null, null);
        shot.x = x;
        shot.y = y;
        return shot;
    }

    public static Projectile bomb(double x, double y, double speed) {
        Group frameA = SpriteFactory.bombFrames()[0];
        Group frameB = SpriteFactory.bombFrames()[1];
        frameB.setVisible(false);
        Projectile bomb = new Projectile(Kind.BOMB, speed, new Group(frameA, frameB), 3, 7, frameA, frameB);
        bomb.x = x;
        bomb.y = y;
        return bomb;
    }

    /** Advances the shot and animates squiggle frames (bomb only). */
    public void update(double dt) {
        y += (kind == Kind.BULLET ? -speed : speed) * dt;
        if (kind == Kind.BOMB) {
            animClock += dt;
            if (animClock >= 0.1) {
                animClock -= 0.1;
                boolean showA = frameA.isVisible();
                frameA.setVisible(!showA);
                frameB.setVisible(showA);
            }
        }
    }

    /** Applies the sprite position (snapped to whole game pixels). */
    public void render() {
        node.setTranslateX(Math.round(x));
        node.setTranslateY(Math.round(y));
    }
}
