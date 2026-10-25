package com.spaceinvaders.view;

import java.util.ArrayList;
import java.util.List;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.model.Player;
import com.spaceinvaders.sprites.SpriteFactory;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Retro HUD drawn inside the scaled playfield: score line on top, green floor
 * line, wave counter and reserve cannon icons at the bottom - like the arcade
 * version's screen furniture.
 */
public final class Hud {
    public final Group node = new Group();
    private final Text scoreValue = text(Constants.HUD_WHITE, 8);
    private final Text hiValue = text(Constants.HUD_WHITE, 8);
    private final Text waveValue = text(Constants.PLAYER_GREEN, 8);
    private final Text livesValue = text(Constants.PLAYER_GREEN, 8);
    private final List<Group> lifeIcons = new ArrayList<>();

    public Hud() {
        createText("SCORE<1>", 8, 9, Constants.PLAYER_GREEN);
        createText("HI-SCORE", 152, 9, Constants.PLAYER_GREEN);
        node.getChildren().addAll(scoreValue, hiValue, waveValue, livesValue);

        Rectangle floor = new Rectangle(0, Constants.FLOOR_Y, Constants.GAME_W, 1);
        floor.setFill(Constants.PLAYER_GREEN);
        node.getChildren().add(floor);
        waveValue.setX(8);
        waveValue.setY(Constants.GAME_H - 7);
        livesValue.setX(160);
        livesValue.setY(Constants.GAME_H - 7);

        // Reserve cannon icons, right-aligned under the floor line
        int iconArea = 3 * Player.W + 2 * 2;
        int startX = Constants.GAME_W - 8 - iconArea;
        for (int i = 0; i < 3; i++) {
            Group icon = SpriteFactory.player();
            icon.setTranslateX(startX + i * (Player.W + 2));
            icon.setTranslateY(Constants.FLOOR_Y + 2);
            lifeIcons.add(icon);
            node.getChildren().add(icon);
        }
        update(0, 0, 1, 3);
    }

    public void update(int score, int hiScore, int wave, int lives) {
        scoreValue.setText(String.format("%05d", score));
        hiValue.setText(String.format("%05d", hiScore));
        waveValue.setText(String.format("WAVE %02d", wave));
        livesValue.setText(String.valueOf(lives));
        int reserves = Math.max(0, Math.min(3, lives - 1));
        for (int i = 0; i < 3; i++) {
            lifeIcons.get(i).setVisible(i < reserves);
        }
    }

    private void createText(String label, double x, double baselineY, javafx.scene.paint.Color color) {
        Text text = text(color, 8);
        text.setText(label);
        text.setX(x);
        text.setY(baselineY);
        node.getChildren().add(text);
    }

    private static Text text(javafx.scene.paint.Color color, double size) {
        Text text = new Text();
        text.setFont(Font.font("Monospaced", FontWeight.BOLD, size));
        text.setFill(color);
        return text;
    }
}
