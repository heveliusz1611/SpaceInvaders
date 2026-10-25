package com.spaceinvaders;

import com.spaceinvaders.config.Constants;
import com.spaceinvaders.view.GamePanel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Entry point: opens an arcade-sized window with a black playfield and runs
 * the fixed-timestep-less (pulse-driven) game loop.
 */
public class SpaceInvadersGame extends Application {

    @Override
    public void start(Stage stage) {
        GamePanel panel = new GamePanel();
        int width = (int) (Constants.GAME_W * panel.getScale());
        int height = (int) (Constants.GAME_H * panel.getScale());
        Scene scene = new Scene(panel, width, height, Color.BLACK);
        panel.attachInput(scene);

        stage.setTitle("SPACE INVADERS");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        panel.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
