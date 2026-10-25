package com.spaceinvaders.sprites;

/**
 * Pixel bitmaps ('X' = opaque pixel, '.' = transparent) recreated from the
 * 1978 arcade ROM sprite shapes. Every frame of a sprite must share the
 * same dimensions so the animated frames can overlap cleanly.
 */
public final class SpriteData {
    private SpriteData() {}

    public static final String[] SQUID_A = {
            "...XX...",
            "..XXXX..",
            ".XXXXXX.",
            "XX.XX.XX",
            "XXXXXXXX",
            "..X..X..",
            ".X.XX.X.",
            "X.X..X.X"
    };

    public static final String[] SQUID_B = {
            "...XX...",
            "..XXXX..",
            ".XXXXXX.",
            "XX.XX.XX",
            "XXXXXXXX",
            ".X.XX.X.",
            "X......X",
            ".X....X."
    };

    public static final String[] CRAB_A = {
            "..X.....X..",
            "...X...X...",
            "..XXXXXXX..",
            ".XX.XXX.XX.",
            "XXXXXXXXXXX",
            "X.XXXXXXX.X",
            "X.X.....X.X",
            "...XX.XX..."
    };

    public static final String[] CRAB_B = {
            "..X.....X..",
            "X..X...X..X",
            "X.XXXXXXX.X",
            "XXX.XXX.XXX",
            "XXXXXXXXXXX",
            ".XXXXXXXXX.",
            "..X.....X..",
            ".X.......X."
    };

    public static final String[] OCTOPUS_A = {
            "....XXXX....",
            ".XXXXXXXXXX.",
            "XXXXXXXXXXXX",
            "XXX..XX..XXX",
            "XXXXXXXXXXXX",
            "...XX..XX...",
            "..XX.XX.XX..",
            "XX........XX"
    };

    public static final String[] OCTOPUS_B = {
            "....XXXX....",
            ".XXXXXXXXXX.",
            "XXXXXXXXXXXX",
            "XXX..XX..XXX",
            "XXXXXXXXXXXX",
            "..XXX..XXX..",
            ".XX..XX..XX.",
            "..XX....XX.."
    };

    public static final String[] CANNON = {
            "......X......",
            ".....XXX.....",
            ".....XXX.....",
            ".XXXXXXXXXXX.",
            "XXXXXXXXXXXXX",
            "XXXXXXXXXXXXX",
            "XXXXXXXXXXXXX",
            "XXXXXXXXXXXXX"
    };

    public static final String[] UFO = {
            ".....XXXXXX.....",
            "...XXXXXXXXXX...",
            "..XXXXXXXXXXXX..",
            ".XX.XX.XX.XX.XX.",
            "XXXXXXXXXXXXXXXX",
            "..XXX.XXXX.XXX..",
            "...X........X..."
    };

    public static final String[] EXPLOSION = {
            "....X...X....",
            ".....X.X.....",
            ".X.........X.",
            "..X........X.",
            "XX....X....XX",
            "..X........X.",
            ".X.........X.",
            "....X...X...."
    };

    public static final String[] CANNON_DEATH_A = {
            "..X...X...X..",
            "X..X..X..X..X",
            "..X..X.X..X..",
            "X.......X...X",
            "...X.....X...",
            "X..X.....X..X",
            "..X..X.X..X..",
            "X.....X.....X"
    };

    public static final String[] CANNON_DEATH_B = {
            "...X.....X...",
            ".X..X...X..X.",
            "X....X.X....X",
            "..X.......X..",
            "X...X...X...X",
            "..X.......X..",
            "X....X.X....X",
            ".X..X...X..X."
    };

    public static final String[] BOMB_A = {
            ".X.",
            "X..",
            ".X.",
            "..X",
            ".X.",
            "X..",
            ".X."
    };

    public static final String[] BOMB_B = {
            ".X.",
            "..X",
            ".X.",
            "X..",
            ".X.",
            "..X",
            ".X."
    };

    public static final String[] SHIELD = {
            "....XXXXXXXXXXXXXX....",
            "...XXXXXXXXXXXXXXXX...",
            "..XXXXXXXXXXXXXXXXXX..",
            ".XXXXXXXXXXXXXXXXXXXX.",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXXXXXXXXXXXXXXXXXX",
            "XXXXXX..........XXXXXX",
            "XXXXX............XXXXX",
            "XXXXX............XXXXX",
            "XXXXX............XXXXX",
            "XXXXX............XXXXX",
            "XXXXX............XXXXX"
    };

    /** Sprite width in game pixels (longest row). */
    public static int width(String[] pattern) {
        int w = 0;
        for (String row : pattern) w = Math.max(w, row.length());
        return w;
    }

    /** Sprite height in game pixels (row count). */
    public static int height(String[] pattern) {
        return pattern.length;
    }
}
