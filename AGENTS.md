# AGENTS.md

Entry point for AI agents and new contributors working in this repository.

## Before adding any code

Read [.agents/reference/CLEAN_CODE.md](.agents/reference/CLEAN_CODE.md) and follow its principles.
Every change is expected to respect them: small focused functions, descriptive names, named
constants instead of magic numbers, comments that explain intent rather than repeat the code,
and the boy scout rule.

## What this repository is

A 3D game base built with jMonkeyEngine, used to learn and experiment with game development.
The current goals (see [README.md](README.md)) are:

- a humanoid character with controls and animations that can interact with the environment
- physics
- a basic settings menu

Current state:

- A grass ground plane, directional and ambient lighting.
- A humanoid GLB model animated through `AnimComposer` (idle, run, jump clips).
- Action-game controls in the style of Elden Ring or Skyrim: the mouse always turns the camera,
  WASD moves relative to the camera, the character turns to face where it runs, Left Shift
  sprints, Space plays the jump animation (no physics yet). Holding left click lets the mouse
  tilt the camera up and down.
- A startup settings dialog that remembers the player's choices.
- In-game profiling: F6 toggles the detailed profiler, F5 the stats overlay.

## Technical stack

| Area | Choice |
|------|--------|
| Language | Java 21 |
| Build | Maven |
| Engine | jMonkeyEngine 3.8.1-stable (core, desktop, lwjgl3, plugins, awt-dialogs) |
| Logging | Log4j2 2.25.3, configured in `src/main/resources/log4j2.xml` |
| Boilerplate | Lombok (`@Log4j2`) |
| Tests | JUnit 3.8.1 (`junit.framework.TestCase`) |
| Coverage | JaCoCo |
| Static analysis | Checkstyle, PMD 7, SpotBugs with find-sec-bugs |
| CI | GitHub Actions (`.github/workflows/build.yml`) and SonarQube Cloud |

## Repository layout

- `src/main/java/org/project/r0kshan/Gamebase.java`: the application. It extends
  `SimpleApplication` and wires the scene, the camera and the controls. Split it into jME
  `AppState` classes as it grows rather than adding more to it.
- `src/main/resources/assets/`: models (`Models/Humanoid/*.glb`), materials and textures.
- `src/test/java/org/project/r0kshan/`: unit tests for logic that does not need a display.
- `config/`: Checkstyle, PMD and SpotBugs rulesets and exclusions.

## Build, check and run

- Full check, as CI runs it: `CI=true mvn -B verify`.
  The PMD and SpotBugs profiles only activate when `CI=true` is set, so a plain `mvn verify`
  can pass locally while CI fails. Always run the `CI=true` form before saying a change is clean.
- Run the game: start `org.project.r0kshan.Gamebase` from the IDE. On macOS do not pass
  `-XstartOnFirstThread`; the game switches to the async GLFW build itself.

## Conventions

- Work on a branch and open a pull request against `main`.
- Commit messages follow Conventional Commits: `feat:`, `fix:`, `chore:`, `docs:`.
- Lines are at most 100 characters (Checkstyle and PMD both enforce it).
- Keep logic that can be tested without a display in small package-private methods marked
  `/* default */` (required by PMD), and cover it in `src/test`. `Gamebase.java` is excluded
  from coverage because it needs a display.
- Key mappings use physical key positions: `KeyInput.KEY_W` is the Z key on an AZERTY keyboard.
- Tuning values (speeds, camera sensitivity, tilt limits) are named constants at the top of
  `Gamebase`; change them there rather than adding inline numbers.
