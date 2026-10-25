# Space Invaders

A classic, colorized Space Invaders arcade game ("the 1980s cabinet" feel) written in pure JavaFX. The sprites are recreated from the original 1978 pixel layouts and drawn as JavaFX graphics nodes - no image files.

## Features

- **Single player** cannon vs a marching invader grid of 11x5 enemies
- **Retro pixels & original colors** - 224x256 arcade layout scaled crisply to the screen, squid/crab/octopus in their classic palette, green bunkers and floor line, red mystery saucer
- **Fixed difficulty per enemy type** - squid 30 pts, crab 20 pts, octopus 10 pts; values and pace are constants, and the grid speeds up the classic way as invaders are destroyed
- **Pure JavaFX graphics** - every sprite is a group of `Rectangle` nodes; no images, no third-party libraries
- **Basic score & lives** - score line, hi-score, 3 lives, bonus life at 1500, reserve cannon icons
- **Reloadable alien grid** - clear the wave and a fresh formation reloads and resumes the march
- Destructible pixel-level bunkers (shots carve craters from below, bombs from above)
- Mystery UFO worth 50/100/150/300 (every 15th shot drops the 300-pointer, arcade-accurate)

## Controls

| Key | Action |
| --- | --- |
| Left / Right (or A / D) | Move cannon |
| Space | Fire (one shot on screen at a time, like the original) |
| P | Pause / resume |
| Enter or Space | Start / play again from the title or game-over screen |

## Tech Stack

- **Java 21**
- **JavaFX 21** (via the OpenJFX Gradle plugin)
- **Gradle** – build tool (with the Gradle Wrapper, no local Gradle install required)
- **JUnit 5** – unit testing
- **JaCoCo** – code coverage reporting

## Requirements

- JDK 17 or later (the compiler targets Java 21; a JDK 25 works too)

## Getting Started

### Build the project

```bash
./gradlew build
```

On Windows, use:

```bash
gradlew.bat build
```

### Run the game

```bash
./gradlew run
```

Or run the JAR produced by the build directly:

```bash
java --module-path <javafx-sdk>/lib --add-modules javafx.controls -jar build/libs/space-invaders-1.0.0.jar
```

### Run tests

```bash
./gradlew test
```

### Generate a code coverage report

```bash
./gradlew jacocoTestReport
```

The report is written to `build/reports/jacoco/test/html/index.html`.

## Project Structure

```
space-invaders/
├── build.gradle            # Build configuration (JavaFX plugin)
├── settings.gradle         # Gradle project settings
├── gradlew / gradlew.bat   # Gradle wrapper scripts
├── gradle/wrapper/         # Gradle wrapper JAR & properties
├── src/
│   ├── main/java/          # Game source code
│   └── test/java/          # Unit tests
└── build/                  # Build output (generated)
```

Main packages:

- `com.spaceinvaders` – `SpaceInvadersGame` (application entry point)
- `com.spaceinvaders.config` – `Constants`: fixed arcade screen layout, speeds and scoring values
- `com.spaceinvaders.model` – game entities (`Alien`, `AlienGrid`, `Player`, `Projectile`, `Shield`, `Ufo`)
- `com.spaceinvaders.sprites` – pixel-art asset data (`SpriteData`) and the node-tree builder (`SpriteFactory`)
- `com.spaceinvaders.view` – JavaFX UI: the `GamePanel` playfield/game loop and the `Hud`
- `com.spaceinvaders.input` – `KeyInput`: keyboard state and per-frame pressed buffer

## License

This project is licensed under the [MIT License](LICENSE).
