package com.spaceinvaders.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.input.KeyInput;
import com.spaceinvaders.model.Alien;
import com.spaceinvaders.model.AlienGrid;
import com.spaceinvaders.model.AlienKind;
import com.spaceinvaders.model.Player;
import com.spaceinvaders.model.Projectile;
import com.spaceinvaders.model.Shield;
import com.spaceinvaders.model.Ufo;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

/**
 * The playfield and game loop. Everything lives in a 224x256 "game pixel"
 * coordinate space that is uniformly scaled to the screen, so sprites stay
 * crisp retro squares at any window scale.
 */
public final class GamePanel extends Parent {
    private enum State { TITLE, PLAYING, PLAYER_DYING, WAVE_CLEAR, GAME_OVER }

    private final Random rng = new Random();
    private State state = State.TITLE;
    private boolean paused;

    // Scene graph
    private final Group content = new Group();   // playfield, uniformly scaled
    private final Group gridRoot = new Group();  // every invader sprite
    private final Group overlays = new Group();
    private final double scale;

    // Wiring & entities
    private final KeyInput input = new KeyInput();
    private final Hud hud = new Hud();
    private final AlienGrid grid = new AlienGrid();
    private final List<Alien> aliens = new ArrayList<>();
    private final List<Shield> shields = new ArrayList<>();
    private final Player player = new Player();
    private final List<Projectile> bullets = new ArrayList<>();
    private final List<Projectile> bombs = new ArrayList<>();
    private final Ufo ufo = new Ufo();
    private final List<Fx> fxList = new ArrayList<>();

    // Timing
    private final AnimationTimer loop;
    private long lastNanos;
    private double gridStepAcc;
    private double bombTimer = 1.5;
    private double ufoTimer;
    private double waveClearT;
    private double deathT;
    private double deathFxT;
    private double blinkClock;
    private boolean invasionReached;
    private boolean lastUfoFromRight;
    private int totalShots;

    // Score state
    private int score;
    private int lives = 3;
    private int wave;
    private int hiScore;
    private boolean extraLifeGiven;

    // Overlay bits
    private final List<Text> blinkTexts = new ArrayList<>();
    private Group titleOverlay;
    private Group gameOverOverlay;
    private Group pauseOverlay;
    private Text waveMsg;
    private Group deathFx;

    /** A transient effect node that disappears once its ttl runs out. */
    private static final class Fx {
        final Node node;
        double ttl;
        Fx(Node node, double ttl) {
            this.node = node;
            this.ttl = ttl;
        }
    }

    public GamePanel() {
        this.scale = computeScale();
        content.getTransforms().add(new Scale(scale, scale));
        getChildren().add(content);

        buildGridNodes();
        applyGridPosition();
        content.getChildren().add(gridRoot);
        buildShields();
        content.getChildren().addAll(ufo.node, player.node, hud.node, overlays);
        buildOverlays();
        ufoTimer = Constants.UFO_RELOAD_MIN;

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = Math.min(0.05, (now - lastNanos) / 1e9);
                lastNanos = now;
                update(dt);
                input.endFrame();
            }
        };
    }

    /** Attaches keyboard handling to the scene (one-time). */
    public void attachInput(Scene scene) {
        input.attach(scene);
    }

    public double getScale() {
        return scale;
    }

    public void start() {
        lastNanos = System.nanoTime();
        loop.start();
    }

    /** Chooses an integer pixel scale that fits the main screen height. */
    private static double computeScale() {
        try {
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            double usable = screen.getHeight() - 90;
            return Math.max(2, Math.min(4, Math.floor(usable / Constants.GAME_H)));
        } catch (Throwable headless) {
            return Constants.DEFAULT_SCALE;
        }
    }

    // ---------------------------------------------------------------- build

    private void buildGridNodes() {
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                Alien alien = new Alien(row, col);
                aliens.add(alien);
                gridRoot.getChildren().add(alien.node);
            }
        }
    }

    private void buildShields() {
        for (Shield old : shields) {
            content.getChildren().remove(old.node);
        }
        shields.clear();
        for (int i = 0; i < Constants.SHIELD_COUNT; i++) {
            Shield shield = new Shield(Constants.SHIELD_LEFT0 + i * Constants.SHIELD_PITCH, Constants.SHIELD_Y);
            shields.add(shield);
            content.getChildren().add(shield.node);
        }
    }

    private void buildOverlays() {
        titleOverlay = new Group();
        titleOverlay.getChildren().add(caption("PLAY", 98, 96, Constants.HUD_WHITE, 8));
        titleOverlay.getChildren().add(caption("SPACE", 42, 116, AlienKind.SQUID.color, 14));
        titleOverlay.getChildren().add(caption("INVADERS", 76, 116, AlienKind.CRAB.color, 14));
        Text prompt = caption("PRESS ENTER TO PLAY", 33, 152, Constants.PLAYER_GREEN, 8);
        blinkTexts.add(prompt);
        titleOverlay.getChildren().add(prompt);
        addLegendRow(titleOverlay, AlienKind.SQUID, "= 30 POINTS", 168);
        addLegendRow(titleOverlay, AlienKind.CRAB, "= 20 POINTS", 184);
        addLegendRow(titleOverlay, AlienKind.OCTOPUS, "= 10 POINTS", 200);
        overlays.getChildren().add(titleOverlay);

        gameOverOverlay = new Group();
        gameOverOverlay.getChildren().add(caption("GAME OVER", 76, 120, Constants.UFO_RED, 12));
        Text againPrompt = caption("PRESS ENTER TO PLAY AGAIN", 27, 150, Constants.PLAYER_GREEN, 8);
        blinkTexts.add(againPrompt);
        gameOverOverlay.getChildren().add(againPrompt);
        gameOverOverlay.setVisible(false);
        overlays.getChildren().add(gameOverOverlay);

        pauseOverlay = new Group();
        pauseOverlay.getChildren().add(caption("PAUSED", 92, 130, Constants.HUD_WHITE, 10));
        pauseOverlay.setVisible(false);
        overlays.getChildren().add(pauseOverlay);

        waveMsg = caption("", 92, 130, Constants.PLAYER_GREEN, 8);
        waveMsg.setVisible(false);
        overlays.getChildren().add(waveMsg);
    }

    private void addLegendRow(Group overlay, AlienKind kind, String label, double topY) {
        Group sprite = SpriteFactory.alien(kind, false);
        sprite.setTranslateX(86 - kind.width());
        sprite.setTranslateY(topY);
        overlay.getChildren().add(sprite);
        overlay.getChildren().add(caption(label, 95, topY + 7, kind.color, 8));
    }

    private Text caption(String content, double x, double baselineY, Color color, double size) {
        Text text = new Text(content);
        text.setFont(Font.font("Monospaced", FontWeight.BOLD, size));
        text.setFill(color);
        text.setX(x);
        text.setY(baselineY);
        return text;
    }

    // ---------------------------------------------------------------- loop

    private void update(double dt) {
        if (state == State.PLAYING && input.pressed(KeyCode.P)) {
            paused = !paused;
            pauseOverlay.setVisible(paused);
        }
        if (paused) {
            return;
        }

        blinkClock += dt;
        boolean blinkOn = (blinkClock / 0.4) % 2 < 1;
        for (Text b : blinkTexts) {
            b.setVisible(blinkOn);
        }

        switch (state) {
            case TITLE, GAME_OVER -> {
                if (input.pressed(KeyCode.ENTER) || input.pressed(KeyCode.SPACE)) {
                    startGame();
                }
            }
            case PLAYING -> {
                updatePlayer(dt);
                spawnBombs(dt);
                updateProjectiles(dt);
                updateUfo(dt);
                updateGrid(dt);
                if (state == State.PLAYING) {
                    checkBulletCollisions();
                    checkBombCollisions();
                }
                checkExtraLife();
            }
            case PLAYER_DYING -> {
                deathT -= dt;
                deathFxT += dt;
                animatePlayerDeathFx();
                if (deathT <= 0) {
                    afterPlayerDeath();
                }
            }
            case WAVE_CLEAR -> {
                waveClearT -= dt;
                if (waveClearT <= 0) {
                    startWave(false);
                }
            }
        }
        updateFx(dt);
    }

    // ---------------------------------------------------------------- state

    private void startGame() {
        score = 0;
        lives = 3;
        wave = 0;
        extraLifeGiven = false;
        invasionReached = false;
        totalShots = 0;
        clearProjectiles();
        clearFx();
        ufo.despawn();
        buildShields();
        player.respawn();
        player.node.setVisible(true);
        titleOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);
        bombTimer = 1.2;
        ufoTimer = Constants.UFO_RELOAD_MIN;
        startWave(false);
    }

    /** Reloads the alien grid and resumes play (the "reloadable grid"). */
    private void startWave(boolean firstWave) {
        grid.reset();
        for (Alien alien : aliens) {
            alien.reset();
        }
        applyGridPosition();
        gridStepAcc = 0;
        invasionReached = false;
        wave++;
        waveMsg.setText("WAVE " + wave);
        waveMsg.setVisible(true);
        hud.update(score, hiScore, wave, lives);
        state = State.PLAYING;
    }

    private void updatePlayer(double dt) {
        double dx = 0;
        if (input.held(KeyCode.LEFT) || input.held(KeyCode.A)) {
            dx -= 1;
        }
        if (input.held(KeyCode.RIGHT) || input.held(KeyCode.D)) {
            dx += 1;
        }
        if (dx != 0) {
            player.move(dx * Constants.PLAYER_SPEED * dt);
        }
        if (input.held(KeyCode.SPACE) && bullets.size() < 1) {
            fire();
        }
    }

    private void fire() {
        totalShots++;
        Projectile bullet = Projectile.bullet(player.centerX() - 0.5, Constants.PLAYER_Y - 4);
        bullets.add(bullet);
        bullet.render();
        content.getChildren().add(bullet.node);
    }

    private void updateGrid(double dt) {
        gridStepAcc += dt;
        while (state == State.PLAYING) {
            double interval = AlienGrid.stepInterval(grid.aliveCount());
            if (gridStepAcc < interval) {
                break;
            }
            gridStepAcc -= interval;
            grid.step();
            applyGridPosition();
            for (Alien alien : aliens) {
                if (alien.isAlive()) {
                    alien.toggleFrame();
                }
            }
            // The march reached the cannon row: automatic loss, like the arcade.
            double bottomWorld = Constants.GRID_TOP_Y + grid.getTy() + grid.bottomLocalY();
            if (bottomWorld >= Constants.PLAYER_Y + 4 && grid.aliveCount() > 0) {
                invasionReached = true;
                lives = 0;
                hud.update(score, hiScore, wave, lives);
                killPlayer();
                return;
            }
        }
        if (grid.aliveCount() == 0 && state == State.PLAYING) {
            clearProjectiles();
            state = State.WAVE_CLEAR;
            waveClearT = Constants.WAVE_CLEAR_PAUSE;
        }
    }

    private void applyGridPosition() {
        gridRoot.setTranslateX(Constants.GRID_START_X + grid.getTx());
        gridRoot.setTranslateY(Constants.GRID_TOP_Y + grid.getTy());
    }

    private void spawnBombs(double dt) {
        bombTimer -= dt;
        if (bombTimer > 0 || bombs.size() >= 3 || grid.aliveCount() == 0) {
            return;
        }
        bombTimer = Constants.BOMB_RELOAD_MIN + rng.nextDouble() * Constants.BOMB_RELOAD_RANGE;
        int[] spot = grid.randomBottomAlien(rng);
        if (spot == null) {
            return;
        }
        Alien alien = aliens.get(spot[0] * Constants.GRID_COLS + spot[1]);
        double x = alienWorldX(alien) + alien.kind.width() / 2.0 - 1.5;
        double y = alienWorldY(alien) + alien.kind.height();
        double speed = Constants.BOMB_BASE_SPEED + rng.nextInt(Constants.BOMB_SPEED_JITTER);
        Projectile bomb = Projectile.bomb(x, y, speed);
        bombs.add(bomb);
        bomb.render();
        content.getChildren().add(bomb.node);
    }

    private void updateProjectiles(double dt) {
        for (Projectile bullet : bullets) {
            bullet.update(dt);
            bullet.render();
        }
        for (Projectile bomb : bombs) {
            bomb.update(dt);
            bomb.render();
        }
    }

    private void updateUfo(double dt) {
        ufoTimer -= dt;
        if (ufoTimer <= 0 && !ufo.active && grid.aliveCount() >= 8) {
            lastUfoFromRight = !lastUfoFromRight;
            ufo.spawn(lastUfoFromRight ? -1 : 1);
            ufoTimer = Constants.UFO_RELOAD_MIN + rng.nextDouble() * Constants.UFO_RELOAD_RANGE;
        }
        if (ufo.active) {
            ufo.update(dt);
        }
    }

    // ------------------------------------------------------------ collisions

    private void checkBulletCollisions() {
        Iterator<Projectile> it = bullets.iterator();
        while (it.hasNext()) {
            Projectile bullet = it.next();
            if (bullet.y <= Constants.UFO_Y - 8) {
                content.getChildren().remove(bullet.node); // flew off the top
                it.remove();
                continue;
            }
            if (hitShield(bullet, true) || hitAlien(bullet) || hitUfo(bullet)) {
                it.remove();
            }
        }
    }

    private void checkBombCollisions() {
        Iterator<Projectile> it = bombs.iterator();
        while (it.hasNext()) {
            Projectile bomb = it.next();
            if (bomb.y >= Constants.FLOOR_Y - bomb.h) {
                addExplosion(bomb.x + bomb.w / 2, Constants.FLOOR_Y - 4, Constants.HUD_WHITE);
                content.getChildren().remove(bomb.node);
                it.remove();
                continue;
            }
            if (hitShield(bomb, false)) {
                it.remove();
                continue;
            }
            if (overlaps(bomb.x, bomb.y, bomb.w, bomb.h,
                    player.x, Constants.PLAYER_Y, Player.W, Player.H)) {
                content.getChildren().remove(bomb.node);
                it.remove();
                killPlayer();
                return;
            }
        }
    }

    private boolean hitShield(Projectile shot, boolean fromBelow) {
        for (Shield shield : shields) {
            int[] cell = shield.hit(shot.x, shot.y, shot.w, shot.h, fromBelow);
            if (cell == null) {
                continue;
            }
            if (fromBelow) {
                shield.bulletImpact(cell[0], cell[1]);
            } else {
                shield.bombImpact(cell[0], cell[1]);
            }
            content.getChildren().remove(shot.node);
            return true;
        }
        return false;
    }

    private boolean hitAlien(Projectile bullet) {
        for (Alien alien : aliens) {
            if (!alien.isAlive()) {
                continue;
            }
            if (!overlaps(bullet.x, bullet.y, bullet.w, bullet.h,
                    alienWorldX(alien), alienWorldY(alien), alien.kind.width(), alien.kind.height())) {
                continue;
            }
            grid.kill(alien.row, alien.col);
            alien.kill();
            addExplosion(alienWorldX(alien) + alien.kind.width() / 2.0,
                    alienWorldY(alien) + alien.kind.height() / 2.0, alien.kind.color);
            addScore(alien.kind.points);
            return true;
        }
        return false;
    }

    private boolean hitUfo(Projectile bullet) {
        if (!ufo.active
                || !overlaps(bullet.x, bullet.y, bullet.w, bullet.h, ufo.x, Constants.UFO_Y, Ufo.W, Ufo.H)) {
            return false;
        }
        int value = scoreUfo();
        addExplosion(ufo.x + Ufo.W / 2.0, Constants.UFO_Y + Ufo.H / 2.0, Constants.UFO_RED);
        Text points = caption(String.valueOf(value), ufo.x + 4, Constants.UFO_Y + Ufo.H + 9, Constants.UFO_RED, 8);
        content.getChildren().add(points);
        fxList.add(new Fx(points, 1.2));
        ufo.despawn();
        addScore(value);
        return true;
    }

    /** Classic mystery scoring: every 15th shot drops the 300-point saucer. */
    private int scoreUfo() {
        if (totalShots % 15 == 0) {
            return Constants.UFO_SCORE_EXTRA;
        }
        int[] table = {50, 100, 150};
        return table[rng.nextInt(table.length)];
    }

    private static boolean overlaps(double ax, double ay, double aw, double ah,
                                    double bx, double by, double bw, double bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private double alienWorldX(Alien alien) {
        return Constants.GRID_START_X + grid.getTx() + alien.localX();
    }

    private double alienWorldY(Alien alien) {
        return Constants.GRID_TOP_Y + grid.getTy() + alien.localY();
    }

    // ---------------------------------------------------------------- score

    private void addScore(int points) {
        score += points;
        if (score > hiScore) {
            hiScore = score;
        }
        hud.update(score, hiScore, wave, lives);
    }

    private void checkExtraLife() {
        if (!extraLifeGiven && score >= Constants.EXTRA_LIFE_AT) {
            extraLifeGiven = true;
            if (lives < 6) {
                lives++;
            }
            hud.update(score, hiScore, wave, lives);
            Text bonus = caption("EXTRA LIFE", 78, 150, Constants.PLAYER_GREEN, 8);
            content.getChildren().add(bonus);
            fxList.add(new Fx(bonus, 1.4));
        }
    }

    // ---------------------------------------------------------------- death

    private void killPlayer() {
        player.node.setVisible(false);
        clearProjectiles();
        deathT = Constants.PLAYER_DEATH_TIME;
        deathFxT = 0;
        Group frameA = SpriteFactory.playerDeathFrames()[0];
        Group frameB = SpriteFactory.playerDeathFrames()[1];
        frameB.setVisible(false);
        deathFx = new Group(frameA, frameB);
        deathFx.setTranslateX(Math.round(player.x));
        deathFx.setTranslateY(Constants.PLAYER_Y);
        content.getChildren().add(deathFx);
        state = State.PLAYER_DYING;
    }

    private void animatePlayerDeathFx() {
        boolean showA = (deathFxT / 0.15) % 2 < 1;
        deathFx.getChildren().get(0).setVisible(showA);
        deathFx.getChildren().get(1).setVisible(!showA);
    }

    private void afterPlayerDeath() {
        content.getChildren().remove(deathFx);
        deathFx = null;
        if (lives <= 0) {
            gameOver();
        } else {
            player.respawn();
            player.node.setVisible(true);
            state = State.PLAYING;
        }
    }

    private void gameOver() {
        state = State.GAME_OVER;
        gameOverOverlay.setVisible(true);
        hud.update(score, hiScore, wave, lives);
    }

    // ---------------------------------------------------------------- fx

    private void addExplosion(double centerX, double centerY, Color color) {
        Group boom = SpriteFactory.explosion(color);
        boom.setTranslateX(Math.round(centerX - 6.5)); // explosion sprite is 13 wide
        boom.setTranslateY(Math.round(centerY - 4));
        content.getChildren().add(boom);
        fxList.add(new Fx(boom, 0.16));
    }

    private void updateFx(double dt) {
        Iterator<Fx> it = fxList.iterator();
        while (it.hasNext()) {
            Fx fx = it.next();
            fx.ttl -= dt;
            if (fx.ttl <= 0) {
                content.getChildren().remove(fx.node);
                it.remove();
            }
        }
    }

    private void clearFx() {
        for (Fx fx : fxList) {
            content.getChildren().remove(fx.node);
        }
        fxList.clear();
    }

    private void clearProjectiles() {
        for (Projectile shot : bullets) {
            content.getChildren().remove(shot.node);
        }
        bullets.clear();
        for (Projectile bomb : bombs) {
            content.getChildren().remove(bomb.node);
        }
        bombs.clear();
    }
}
