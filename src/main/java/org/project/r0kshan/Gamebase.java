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
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
  // Action-game controls (Elden Ring / Skyrim style): mouse always looks, WASD moves
  // relative to the camera. Key codes are physical positions (W = Z on AZERTY).
  private static final String FORWARD = "Forward";
  private static final String BACKWARD = "Backward";
  private static final String LEFT = "Left";
  private static final String RIGHT = "Right";
  private static final String SPRINT = "Sprint";
  private static final String JUMP = "Jump";

  private static final String IDLE_ANIM = "Idle";
  private static final String RUNNING_ANIM = "Run_03";
  private static final String JUMP_ANIM = "Jump_Over_Obstacle";
  private static final String MODEL = "model-anim-renamed.glb";

  private static final float MOVE_SPEED = 20f;
  private static final float SPRINT_FACTOR = 1.6f;
  private static final float FACING_TURN_SPEED = 20f; // how fast the character turns to run
  // Camera turn speed per mouse movement (ChaseCamera default is 1): raise it to turn more
  // for the same hand movement
  private static final float CAMERA_TURN_FACTOR = 3f;
  // Camera tilt speed relative to ChaseCamera default: slight vertical slips barely tilt
  private static final float CAMERA_TILT_FACTOR = 0.2f;
  private static final float CAMERA_MAX_TILT = 60f * FastMath.DEG_TO_RAD;
  private Spatial player;
  private AnimComposer composer;

  // Names of the mappings currently held down
  private final Set<String> held = new HashSet<>();
  // Seconds left in the current jump; jump plays once per press, not while held
  private float jumpTimeLeft;
  private final ActionListener actionListener = (name, isPressed, tpf) -> {

    if (isPressed) {
      log.info("Key Pressed: {}", name);
    }

    if (isPressed) {
      held.add(name);
    } else {
      held.remove(name);
    }
    if (JUMP.equals(name) && isPressed && jumpTimeLeft <= 0) {
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

    // ChaseCamera shares one speed for turning and tilting; scale each on its own
    final ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager) {
      @Override
      protected void rotateCamera(final float value) {
        super.rotateCamera(value * CAMERA_TURN_FACTOR);
      }

      @Override
      protected void vRotateCamera(final float value) {
        super.vRotateCamera(value * CAMERA_TILT_FACTOR);
      }
    };
    // No smooth motion: it lerps the distance back slowly, so the camera lags and drifts
    // away while the character runs. Off, it stays at the exact distance every frame.
    chaseCam.setSmoothMotion(false);

    // 1. Horizontal Rotation: start right behind the character
    chaseCam.setDefaultHorizontalRotation(behindAngle(forwardOf(player)));

    // 2. Vertical Rotation:
    // This tilts the camera down so you are looking from slightly above.
    // 0.3f to 0.5f is usually a good "over the shoulder" angle.
    chaseCam.setDefaultVerticalRotation(0.2f);
    chaseCam.setMaxVerticalRotation(CAMERA_MAX_TILT); // default 90 degrees looks straight down

    // 3. Distance and Offsets
    chaseCam.setDefaultDistance(6f);
    chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0)); // Look at head/shoulders

    // 4. Mouse always looks around, no button needed; this also hides the cursor.
    // Trailing is off: the camera stays where the mouse put it when the character turns.
    chaseCam.setDragToRotate(false);
    chaseCam.setTrailingEnabled(false);
  }

  private void initKeys() {
    inputManager.addMapping(FORWARD, new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping(BACKWARD, new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping(SPRINT, new KeyTrigger(KeyInput.KEY_LSHIFT));
    inputManager.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_SPACE));

    inputManager.addListener(actionListener, FORWARD, BACKWARD, LEFT, RIGHT, SPRINT, JUMP);
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
    return movementInput().lengthSquared() > 0;
  }

  /** Held movement keys as x = left, z = forward, relative to the camera. */
  private Vector3f movementInput() {
    final Vector3f input = new Vector3f();
    if (held.contains(FORWARD)) {
      input.z += 1;
    }
    if (held.contains(BACKWARD)) {
      input.z -= 1;
    }
    if (held.contains(LEFT)) {
      input.x += 1;
    }
    if (held.contains(RIGHT)) {
      input.x -= 1;
    }
    return input;
  }

  /** Character facing on the ground plane (models face +Z). */
  private static Vector3f forwardOf(final Spatial spatial) {
    return spatial.getLocalRotation().mult(Vector3f.UNIT_Z).setY(0).normalizeLocal();
  }

  /**
   * ChaseCamera horizontal angle that puts the camera behind a character facing forward.
   * ChaseCamera places the camera at (cos a, sin a) on the XZ plane around the target.
   */
  static float behindAngle(final Vector3f forward) {
    return FastMath.atan2(-forward.z, -forward.x);
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

    // 1. Camera directions on the ground plane, so looking up or down doesn't lift the run
    final Vector3f input = movementInput();
    if (input.lengthSquared() == 0) {
      return;
    }
    final Vector3f camForward = cam.getDirection().clone().setY(0).normalizeLocal();
    final Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();
    final Vector3f walk = camForward.multLocal(input.z).addLocal(camLeft.multLocal(input.x))
        .normalizeLocal();

    // 2. Run that way; S turns the character around toward the camera
    final float speed = held.contains(SPRINT) ? MOVE_SPEED * SPRINT_FACTOR : MOVE_SPEED;
    player.move(walk.mult(speed * tpf));

    // 3. Turn smoothly to face the run direction
    final Quaternion target = new Quaternion();
    target.lookAt(walk, Vector3f.UNIT_Y);
    final Quaternion rotation = player.getLocalRotation().clone();
    rotation.slerp(target, Math.min(1f, FACING_TURN_SPEED * tpf));
    player.setLocalRotation(rotation);
  }
}
