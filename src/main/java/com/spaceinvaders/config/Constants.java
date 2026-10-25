package com.spaceinvaders.config;

import javafx.scene.paint.Color;

/**
 * Fixed gameplay constants laid out on the original 224x256 arcade screen.
 * Difficulty values are fixed constants, exactly like the 1980s cabinet.
 */
public final class Constants {
    private Constants() {}

    // Screen (original arcade resolution, scaled up at render time)
    public static final int GAME_W = 224;
    public static final int GAME_H = 256;
    public static final int DEFAULT_SCALE = 3;

    // Retro palette (cellophane strip colors from the color-cabinet era)
    public static final Color PLAYER_GREEN = Color.web("#38FF38");
    public static final Color HUD_WHITE = Color.web("#F8F8F8");
    public static final Color UFO_RED = Color.web("#FF4136");

    // Alien formation: 11 columns x 5 rows, 16px cells
    public static final int GRID_COLS = 11;
    public static final int GRID_ROWS = 5;
    public static final int CELL_W = 16;
    public static final int CELL_H = 16;
    public static final int GRID_START_X = 24;
    public static final int GRID_TOP_Y = 64;
    public static final int STEP_X = 2;      // sideways step per tick (classic)
    public static final int STEP_DOWN = 8;   // drop when an edge is reached
    public static final int GRID_MARGIN = 3; // screen margin before turning

    // World rows
    public static final int UFO_Y = 32;
    public static final int SHIELD_Y = 192;  // y of the bunker row (top edge)
    public static final int PLAYER_Y = 216;  // y of the cannon (top edge)
    public static final int FLOOR_Y = 239;   // green ground line

    // Speeds: game pixels per second
    public static final double PLAYER_SPEED = 62.0;
    public static final double BULLET_SPEED = 210.0;
    public static final double UFO_SPEED = 22.0;
    public static final int BOMB_BASE_SPEED = 55;
    public static final int BOMB_SPEED_JITTER = 25;

    // Scoring
    public static final int UFO_SCORE_EXTRA = 300; // classic mystery 300-hit rule
    public static final int EXTRA_LIFE_AT = 1500;  // classic bonus life

    // Bunkers: 4 shields, evenly spaced
    public static final int SHIELD_COUNT = 4;
    public static final int SHIELD_PITCH = 55;
    public static final int SHIELD_LEFT0 = 19;

    // Timing (seconds)
    public static final double WAVE_CLEAR_PAUSE = 1.6;
    public static final double PLAYER_DEATH_TIME = 1.1;
    public static final double BOMB_RELOAD_MIN = 0.55;
    public static final double BOMB_RELOAD_RANGE = 0.9;
    public static final double UFO_RELOAD_MIN = 18.0;
    public static final double UFO_RELOAD_RANGE = 12.0;
}
