package com.spaceinvaders.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.util.Random;

/**
 * The original cabinet's sound set, synthesized in code - no audio files.
 *
 * <ul>
 *   <li>four-note march: one low "thump" per grid step, so it speeds up as
 *       invaders are destroyed (the game's heartbeat)</li>
 *   <li>shot: the descending cannon "pew"</li>
 *   <li>invader explosion: the short noisy crunch (also used for the UFO)</li>
 *   <li>player explosion: the long, chaotic crash</li>
 *   <li>UFO: a warbling siren that loops while the saucer is on screen</li>
 *   <li>extra life: a short rising jingle</li>
 * </ul>
 *
 * Samples are built once as 16-bit mono PCM in memory and played through
 * {@link Clip}s. If no audio device is available the class disables itself
 * and the game keeps working silently, exactly like the headless case.
 */
public final class Sounds {
    private Sounds() {}

    private static final float SAMPLE_RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final Random RNG = new Random();

    private static volatile boolean broken;

    private static final byte[] SHOT = renderShot();
    private static final byte[] MARCH_NOTE_0 = renderMarchNote(0);
    private static final byte[] MARCH_NOTE_1 = renderMarchNote(1);
    private static final byte[] MARCH_NOTE_2 = renderMarchNote(2);
    private static final byte[] MARCH_NOTE_3 = renderMarchNote(3);
    private static final byte[] INVADER_BOOM = renderInvaderBoom();
    private static final byte[] PLAYER_BOOM = renderPlayerBoom();
    private static final byte[] UFO_LOOP = ufoLoop();
    private static final byte[] EXTRA_LIFE = renderExtraLife();

    private static Clip ufoClip;

    public static void shot() {
        play(SHOT);
    }

    /** Advance the four-note march; the step index repeats every 4 notes. */
    public static void marchNote(int step) {
        switch (Math.floorMod(step, 4)) {
            case 0 -> play(MARCH_NOTE_0);
            case 1 -> play(MARCH_NOTE_1);
            case 2 -> play(MARCH_NOTE_2);
            default -> play(MARCH_NOTE_3);
        }
    }

    public static void invaderBoom() {
        play(INVADER_BOOM);
    }

    public static void playerBoom() {
        play(PLAYER_BOOM);
    }

    public static void extraLife() {
        play(EXTRA_LIFE);
    }

    public static void ufoStart() {
        if (broken || (ufoClip != null && ufoClip.isRunning())) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, UFO_LOOP, 0, UFO_LOOP.length);
            clip.setLoopPoints(0, -1);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            ufoClip = clip;
        } catch (Throwable t) {
            broken = true;
        }
    }

    /** Stops the UFO warble; safe to call any number of times. */
    public static void ufoStop() {
        if (ufoClip == null) {
            return;
        }
        try {
            ufoClip.stop();
            ufoClip.close();
        } catch (Throwable ignored) {
            // closing a closed line throws; the warble was going to stop anyway
        }
        ufoClip = null;
    }

    // ---------------------------------------------------------------- engine

    private static void play(byte[] data) {
        if (broken) {
            return;
        }
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(Sounds::onLineStopped);
            clip.open(FORMAT, data, 0, data.length);
            clip.start();
        } catch (Throwable t) {
            // audio hiccup (busy line, no device) - skip this sound, keep playing
        }
    }

    private static void onLineStopped(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            try {
                event.getLine().close();
            } catch (Throwable ignored) {
            }
        }
    }

    // ---------------------------------------------------------------- synthesis

    @FunctionalInterface
    private interface SoundFn {
        /** Returns the sample in [-1, 1] for time t in seconds. */
        double sample(double t);
    }

    private static byte[] render(double seconds, SoundFn fn) {
        int n = (int) (seconds * SAMPLE_RATE);
        byte[] data = new byte[2 * n];
        for (int i = 0; i < n; i++) {
            double v = Math.clamp(fn.sample(i / SAMPLE_RATE), -1.0, 1.0);
            int s = (int) (v * 30000);
            data[2 * i] = (byte) s;
            data[2 * i + 1] = (byte) (s >>> 8);
        }
        return data;
    }

    private static double square(double phase) {
        return Math.signum(Math.sin(phase));
    }

    /** Cannon "pew": a square tone sweeping ~1250 Hz down to ~280 Hz with fast decay. */
    private static byte[] renderShot() {
        double T = 0.22;
        double f1 = 1250, f2 = 280;
        double r = Math.pow(f2 / f1, 1.0 / T);
        return render(T, t -> {
            // closed-form phase of an exponential sweep keeps no per-sample state
            double phase = 2 * Math.PI * f1 * (Math.pow(r, t) - 1) / Math.log(r);
            double env = Math.exp(-7 * t);
            return (0.7 * square(phase) + 0.3 * Math.sin(phase)) * env * 0.8;
        });
    }

    /**
     * One march "thump": a low tone with strong harmonics (so it reads on
     * laptop speakers) with a quick kick-like decay. The four notes descend,
     * like the arcade cabinet's heartbeat.
     */
    private static byte[] renderMarchNote(int index) {
        double f = 66 - index * 7; // 66, 59, 52, 45 Hz
        return render(0.10, t -> {
            double p = 2 * Math.PI * f * t;
            double tone = Math.sin(p) + 0.6 * Math.sin(2 * p) + 0.3 * Math.sin(3 * p);
            return tone * Math.exp(-34 * t) / 1.9;
        });
    }

    /** Invader kill: a short, crunchy noise burst that dies out fast. */
    private static byte[] renderInvaderBoom() {
        return noiseBoom(0.30, 9.0, 0.50, false);
    }

    /** Player death: a long, chaotic rumble with faltering volume chunks. */
    private static byte[] renderPlayerBoom() {
        return noiseBoom(0.90, 3.2, 0.14, true);
    }

    /**
     * Low-passed white-noise crash with an exponential decay. The stateful
     * filter is safe because the samples are generated strictly in order.
     */
    private static byte[] noiseBoom(double seconds, double decay, double lpCoeff, boolean faltering) {
        int n = (int) (seconds * SAMPLE_RATE);
        byte[] data = new byte[2 * n];
        double lp = 0;
        for (int i = 0; i < n; i++) {
            double t = i / SAMPLE_RATE;
            lp += lpCoeff * ((RNG.nextDouble() * 2 - 1) - lp);
            double env = Math.exp(-decay * t);
            if (faltering) {
                env *= Math.clamp(0.65 + 0.5 * Math.sin(2 * Math.PI * 17 * t) * Math.sin(2 * Math.PI * 7.3 * t), 0, 1);
            }
            double v = Math.clamp(lp * env, -1.0, 1.0);
            int s = (int) (v * 30000);
            data[2 * i] = (byte) s;
            data[2 * i + 1] = (byte) (s >>> 8);
        }
        return data;
    }

    /**
     * UFO warble: a fast siren weaving around ~900 Hz. The loop is seamless by
     * construction (modulation completes whole cycles and the phase advance is
     * an integer number of periods), so it can loop forever.
     */
    private static byte[] ufoLoop() {
        double T = 0.5;
        double base = 880, depth = 240, warbleHz = 4;
        return render(T, t -> {
            // phase = integral of f(t) = base + depth*sin(2*pi*warbleHz*t)
            double phase = 2 * Math.PI * (base * t - depth * T / (2 * Math.PI * warbleHz) * Math.cos(2 * Math.PI * warbleHz * t));
            return Math.sin(phase) * 0.6;
        });
    }

    /** Rising jingle for the 1500-point bonus life. */
    private static byte[] renderExtraLife() {
        double[] notes = {523.25, 659.25, 783.99, 1046.5}; // C5 E5 G5 C6
        double dur = 0.16, gap = 0.02;
        return render(notes.length * dur + (notes.length - 1) * gap, t -> {
            int idx = (int) (t / (dur + gap));
            if (idx >= notes.length) {
                return 0;
            }
            double local = t - idx * (dur + gap);
            if (local > dur) {
                return 0;
            }
            double env = Math.exp(-5 * local);
            return square(2 * Math.PI * notes[idx] * local) * env * 0.4;
        });
    }
}
