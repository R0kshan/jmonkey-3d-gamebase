package org.project.r0kshan;

import com.jme3.anim.AnimComposer;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.DetailedProfilerState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioListenerState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import lombok.extern.log4j.Log4j2;
import org.lwjgl.system.Configuration;

/**
 * Main class for JMonkey gamebase showcasing a minimal world with basic environnemental
 * interactions.
 */
@Log4j2
// ponytail: entry class wires the whole scene; split into AppStates when it grows
@SuppressWarnings("PMD.ExcessiveImports")
public class Gamebase extends SimpleApplication {

  private static final String TITLE = "Gamebase";
  private static final String ASSETS_PATH = "src/main/resources/assets";
  private static final String RIGHT_KEY = "Right";
  private static final String LEFT_KEY = "Left";
  private static final String UP_KEY = "Up";
  private static final String DOWN_KEY = "Down";
  private static final String SPACE_KEY = "Space";
  private static final String STEER_KEY = "Steer";

  private static final String IDLE_ANIM = "Idle";
  private static final String RUNNING_ANIM = "Run_03";
  private static final String JUMP_ANIM = "Jump_Over_Obstacle";
  private static final String MODEL = "model-anim-renamed.glb";

  private static final float MOVE_SPEED = 20f;
  private Spatial player;
  private AnimComposer composer;

  private boolean leftIsPressed;
  private boolean rightIsPressed;
  private boolean upIsPressed;
  private boolean downIsPressed;
  // Right mouse held: mouse orbits the camera and the character turns to face where it looks
  private boolean steerIsPressed;
  // Seconds left in the current jump; jump plays once per press, not while held
  private float jumpTimeLeft;
  private final ActionListener actionListener = (name, isPressed, tpf) -> {

    if (isPressed) {
      log.info("Key Pressed: {}", name);
    }

    if (LEFT_KEY.equals(name)) {
      leftIsPressed = isPressed;
    }
    if (RIGHT_KEY.equals(name)) {
      rightIsPressed = isPressed;
    }
    if (UP_KEY.equals(name)) {
      upIsPressed = isPressed;
    }
    if (DOWN_KEY.equals(name)) {
      downIsPressed = isPressed;
    }
    if (STEER_KEY.equals(name)) {
      steerIsPressed = isPressed;
    }
    if (SPACE_KEY.equals(name) && isPressed && jumpTimeLeft <= 0) {
      startJump();
    }
    // Update animation state
    updateAnimation();
  };

  /**
   * Constructor for the main class.
   */
  public Gamebase() {
    super(new StatsAppState(), new FlyCamAppState(), new AudioListenerState(),
        new DebugKeysAppState());
  }
  
  /**
   * Main.
   *
   * @param args main arguments
   */
  // AppSettings extends HashMap, but jME's API requires that exact type
  @SuppressWarnings("PMD.LooseCoupling")
  public static void main(String[] args) {
    final boolean macOs = System.getProperty("os.name").startsWith("Mac");
    // macOS sets this variable when the JVM runs with -XstartOnFirstThread
    final boolean firstThread = macOs && System.getenv(
        "JAVA_STARTED_ON_FIRST_THREAD_" + ProcessHandle.current().pid()) != null;
    // LWJGL3 on macOS normally needs -XstartOnFirstThread, which deadlocks the AWT settings
    // dialog. The async GLFW build runs the window off the first thread, so both work.
    if (macOs && !firstThread) {
      Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
    }

    // The dialog saves the player's choices under the title on OK; load them back here
    final AppSettings settings = new AppSettings(true);
    settings.setTitle(TITLE);
    try {
      settings.load(TITLE);
    } catch (BackingStoreException e) {
      log.warn("Could not load saved settings, using defaults: {}", e.getMessage());
    }

    final Gamebase app = new Gamebase();
    app.setSettings(settings);
    if (firstThread) {
      log.warn("Settings dialog skipped: it hangs under -XstartOnFirstThread, which is not "
          + "needed on macOS any more");
      app.setShowSettings(false);
    }
    app.start();
  }

  @Override
  public void simpleInitApp() {


    final Path classesDir;
    try {
      classesDir = Paths.get(
          Gamebase.class.getProtectionDomain().getCodeSource().getLocation().toURI()
      );
    } catch (URISyntaxException e) {
      log.fatal("Failed to resolve asset path: {}", e.getMessage());
      // Tells jME to shut down the display and exit the main loop cleanly
      this.stop();
      return;
    }

    // target/classes (or the jar) sits two levels below the module root
    final Path moduleRoot = classesDir.resolve("../..").normalize();

    final String assetsPath = moduleRoot.resolve(ASSETS_PATH).toString();
    assetManager.registerLocator(assetsPath, FileLocator.class);
    assetManager.registerLocator(assetsPath, ClasspathLocator.class);

    setupLighting();
    createGrassGround();
    createCharacter();
    setupChaseCamera();

    initKeys();

    // Per-frame timings for update, render and each app state/viewport; F6 toggles it
    final DetailedProfilerState profiler = new DetailedProfilerState();
    profiler.setEnabled(false);
    stateManager.attach(profiler);

  }

  /**
   * Sets up basic scene lighting.
   * Necessary to avoid black screen.
   */
  public void setupLighting() {
    // Add directional light (like sun)
    final DirectionalLight directionalLight = new DirectionalLight();
    directionalLight.setDirection(new Vector3f(-0.5f, -1.0f, -0.5f).normalizeLocal());
    directionalLight.setColor(ColorRGBA.White.mult(1.2f));  // Bright white light
    rootNode.addLight(directionalLight);

    // Add ambient light for overall brightness
    final AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.3f));  // Subtle ambient
    rootNode.addLight(ambient);
  }

  /**
   * Creates a large grass ground plane.
   */
  public void createGrassGround() {

    // Create the Geometry for a grass terrain of 100x100 units
    final Quad groundQuad = new Quad(100f, 100f);
    final Geometry groundGeom = new Geometry("GrassGround", groundQuad);

    // Load the Material instance (.j3m)
    final Material grassMat = assetManager.loadMaterial("Materials/GrassGround.j3m");

    // Texture Tiling : without tiling one blade of grass will stretch across the whole floor
    // This repeats the texture 10 times in both directions.
    grassMat.getTextureParam("DiffuseMap").getTextureValue().setWrap(Texture.WrapMode.Repeat);
    groundQuad.scaleTextureCoordinates(new Vector2f(10f, 10f));

    groundGeom.setMaterial(grassMat);

    // Position it
    // Quads are vertical by default, rotate it -90 degrees on the X-axis to lay it flat
    groundGeom.rotate(-FastMath.HALF_PI, 0, 0);
    groundGeom.setLocalTranslation(-50, 0, 50); // Center it


    rootNode.attachChild(groundGeom);
  }

  /**
   * Create the character.
   */
  public void createCharacter() {
    player = assetManager.loadModel("Models/Humanoid/" + MODEL);
    player.setLocalScale(1.0f);
    player.setLocalTranslation(0, 0, 0);
    player.rotate(0, -10f, 0);

    // Use the helper method to search the whole model tree
    composer = findComposer(player);


    if (composer != null) {

      // 1. Wipe out any animations that started automatically
      composer.reset();

      log.info("Success! Found composer on: {}", composer.getSpatial().getName());
      log.info("Available animations: {}", composer.getAnimClipsNames());
      log.info("Available animations layers: {}", composer.getLayerNames());


      // Ensure "Idle" exists before playing
      if (composer.getAnimClipsNames().contains(IDLE_ANIM)) {
        composer.setCurrentAction(IDLE_ANIM); // maps to idle
        // composer.setCurrentAction("Idle_4"); // maps to idle

        log.info("Current animation : {}", composer.getCurrentAction());
      }
    } else {
      log.error("Could not find AnimComposer .glb!");
    }

    rootNode.attachChild(player);
  }

  /**
   * Helper to find AnimComposer anywhere in the model's hierarchy.
   */
  private AnimComposer findComposer(final Spatial spatial) {

    AnimComposer control = spatial.getControl(AnimComposer.class);

    if (control == null && spatial instanceof Node node) {
      control = node.getChildren().stream()
          .map(this::findComposer)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }

    return control;
  }

  /**
   * Setup the chase camera based on player's location.
   */
  public void setupChaseCamera() {
    flyCam.setEnabled(false);

    final ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
    // No smooth motion: it lerps the distance back slowly, so the camera lags and drifts
    // away while the character runs. Off, it stays at the exact distance every frame.
    chaseCam.setSmoothMotion(false);

    // 1. Horizontal Rotation:
    // If you see the front now, change FastMath.PI to 0.
    // If you see the side, try FastMath.HALF_PI (90 degrees).
    chaseCam.setDefaultHorizontalRotation(0);

    // 2. Vertical Rotation:
    // This tilts the camera down so you are looking from slightly above.
    // 0.3f to 0.5f is usually a good "over the shoulder" angle.
    chaseCam.setDefaultVerticalRotation(0.2f);

    // 3. Distance and Offsets
    chaseCam.setDefaultDistance(6f);
    chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0)); // Look at head/shoulders

    // 4. Mouse look only while right mouse is held (cursor hidden meanwhile); a free mouse
    // leaves the camera alone. Trailing is off: an auto-swinging camera fights
    // camera-relative movement, making strafe keys run the character in circles.
    chaseCam.setDragToRotate(true);
    chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
    chaseCam.setTrailingEnabled(false);
  }

  private void initKeys() {
    inputManager.addMapping(UP_KEY, new KeyTrigger(KeyInput.KEY_W)); // Physical Z on AZERTY
    inputManager.addMapping(LEFT_KEY, new KeyTrigger(KeyInput.KEY_A)); // Physical Q on AZERTY
    inputManager.addMapping(DOWN_KEY, new KeyTrigger(KeyInput.KEY_S)); // Physical S
    inputManager.addMapping(RIGHT_KEY, new KeyTrigger(KeyInput.KEY_D)); // Physical D
    inputManager.addMapping(SPACE_KEY, new KeyTrigger(KeyInput.KEY_SPACE));

    // Same button the chase camera rotates on; jME lets both mappings share the trigger
    inputManager.addMapping(STEER_KEY, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

    inputManager.addListener(actionListener, LEFT_KEY, RIGHT_KEY, UP_KEY, DOWN_KEY, SPACE_KEY,
        STEER_KEY);
  }

  private void updateAnimation() {

    log.info("Composer : {}", composer);
    if (composer == null) {
      return;
    }

    log.info("Determine animation");

    // Determine which animation we WANT to play
    String desiredAnim = isMoving() ? RUNNING_ANIM : IDLE_ANIM;
    if (jumpTimeLeft > 0) {
      desiredAnim = JUMP_ANIM;
    }

    // Only change if it's different from the CURRENTLY playing animation
    if (composer.getCurrentAction() == null
        || !composer.getCurrentAction().toString().equals(desiredAnim)) {

      // Safety check: verify the model actually has this animation
      if (composer.getAnimClipsNames().contains(desiredAnim)) {
        composer.setCurrentAction(desiredAnim);

        log.info("Current animation: {}", desiredAnim);
      } else {
        log.error("Error: Model is missing animation: {}", desiredAnim);
      }
    }
  }

  private void startJump() {
    if (composer == null || composer.getAnimClip(JUMP_ANIM) == null) {
      return;
    }
    jumpTimeLeft = (float) composer.getAnimClip(JUMP_ANIM).getLength();
    // Restart the clip from frame 0 even if it was the last action played
    composer.setCurrentAction(JUMP_ANIM);
    composer.setTime(0);
  }

  private boolean isMoving() {
    return upIsPressed || downIsPressed || leftIsPressed || rightIsPressed;
  }

  @Override
  public void simpleUpdate(final float tpf) {
    if (player == null) {
      return;
    }
    if (jumpTimeLeft > 0) {
      jumpTimeLeft -= tpf;
      if (jumpTimeLeft <= 0) {
        updateAnimation(); // back to idle/run once the jump clip has played
      }
    }
    // 1. Get the camera direction, but ignore the Y (up/down) axis
    // so the character doesn't fly into the air when looking up.
    final Vector3f camDir = cam.getDirection().clone().setY(0).normalizeLocal();
    final Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();
    final Vector3f walkDirection = new Vector3f(0, 0, 0);

    // 2. Calculate direction based on which keys are held
    if (upIsPressed) {
      walkDirection.addLocal(camDir);
    }
    if (downIsPressed) {
      walkDirection.subtractLocal(camDir);
    }
    if (leftIsPressed) {
      walkDirection.addLocal(camLeft);
    }
    if (rightIsPressed) {
      walkDirection.subtractLocal(camLeft);
    }

    // 3. Steering: the character faces where the camera looks, so A/D strafe
    if (steerIsPressed) {
      final Quaternion steerRotation = new Quaternion();
      steerRotation.lookAt(camDir, Vector3f.UNIT_Y);
      player.setLocalRotation(steerRotation);
    }

    // 4. If any key is pressed, move (and turn toward the movement unless steering)
    if (walkDirection.length() > 0) {
      walkDirection.normalizeLocal();

      // Move the player spatial
      player.move(walkDirection.mult(MOVE_SPEED * tpf));
      if (steerIsPressed) {
        return;
      }

      // Make the player face the direction they are walking
      // We use slerp for smooth rotation (optional, but looks better)
      final Quaternion lookRotation = new Quaternion();
      lookRotation.lookAt(walkDirection, Vector3f.UNIT_Y);
      final Quaternion rotation = player.getLocalRotation().clone();
      rotation.slerp(lookRotation, Math.min(1f, 10f * tpf));
      player.setLocalRotation(rotation);
    }
  }
}
