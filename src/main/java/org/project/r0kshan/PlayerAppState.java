package org.project.r0kshan;

import com.jme3.anim.AnimComposer;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;

/**
 * Humanoid player: WASD walks relative to the camera, Space jumps, a chase camera follows,
 * and the animation follows the physics state. Requires a {@link BulletAppState}.
 */
@Log4j2
public class PlayerAppState extends BaseAppState implements ActionListener {

  /* default */ static final String IDLE_ANIM = "Idle";
  /* default */ static final String RUN_ANIM = "Run_03";
  /* default */ static final String JUMP_ANIM = "Jump_Over_Obstacle";

  /* default */ static final String FORWARD = "Forward";
  /* default */ static final String BACKWARD = "Backward";
  /* default */ static final String LEFT = "Left";
  /* default */ static final String RIGHT = "Right";
  /* default */ static final String JUMP = "Jump";
  private static final String[] ACTIONS = {FORWARD, BACKWARD, LEFT, RIGHT, JUMP};

  private static final String MODEL = "Models/Humanoid/model-anim-renamed.glb";
  private static final float MOVE_SPEED = 20f;

  // Character capsule, in world units (meters). Tune to the model if it floats or sinks.
  private static final float CHARACTER_RADIUS = 0.4f;
  private static final float CHARACTER_HEIGHT = 1.8f;
  private static final float CHARACTER_MASS = 80f;

  private final Vector3f walkDirection = new Vector3f();
  private final Vector3f camForward = new Vector3f();
  private final Vector3f camLeft = new Vector3f();

  private Spatial player;
  private BetterCharacterControl character;
  private AnimComposer composer;
  private ChaseCamera chaseCam;
  private PhysicsSpace physicsSpace;

  private boolean forward;
  private boolean backward;
  private boolean left;
  private boolean right;

  /**
   * Animation for the current physics state: airborne beats moving beats idle.
   */
  /* default */ static String animationFor(final boolean onGround, final boolean moving) {
    return onGround ? (moving ? RUN_ANIM : IDLE_ANIM) : JUMP_ANIM;
  }

  @Override
  protected void initialize(final Application app) {
    player = app.getAssetManager().loadModel(MODEL);
    composer = findComposer(player);
    if (composer == null) {
      log.error("No AnimComposer in {}, character will not animate", MODEL);
    } else {
      // Drop any animation that started automatically
      composer.reset();
      log.info("Available animations: {}", composer.getAnimClipsNames());
    }

    // The control owns position and facing from now on; never move the spatial directly
    character = new BetterCharacterControl(CHARACTER_RADIUS, CHARACTER_HEIGHT, CHARACTER_MASS);
    player.addControl(character);
    physicsSpace = getState(BulletAppState.class, true).getPhysicsSpace();
    physicsSpace.add(character);

    chaseCam = createChaseCamera(app.getCamera(), app.getInputManager());
  }

  @Override
  protected void onEnable() {
    ((SimpleApplication) getApplication()).getRootNode().attachChild(player);
    chaseCam.setEnabled(true);

    final InputManager input = getApplication().getInputManager();
    input.addMapping(FORWARD, new KeyTrigger(KeyInput.KEY_W)); // Physical Z on AZERTY
    input.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A)); // Physical Q on AZERTY
    input.addMapping(BACKWARD, new KeyTrigger(KeyInput.KEY_S));
    input.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D));
    input.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_SPACE));
    input.addListener(this, ACTIONS);
  }

  @Override
  protected void onDisable() {
    final InputManager input = getApplication().getInputManager();
    input.removeListener(this);
    for (final String action : ACTIONS) {
      input.deleteMapping(action);
    }
    forward = false;
    backward = false;
    left = false;
    right = false;
    character.setWalkDirection(Vector3f.ZERO);

    chaseCam.setEnabled(false);
    player.removeFromParent();
  }

  @Override
  protected void cleanup(final Application app) {
    physicsSpace.remove(character);
  }

  @Override
  public void onAction(final String name, final boolean isPressed, final float tpf) {
    switch (name) {
      case FORWARD -> forward = isPressed;
      case BACKWARD -> backward = isPressed;
      case LEFT -> left = isPressed;
      case RIGHT -> right = isPressed;
      case JUMP -> {
        if (isPressed) {
          character.jump();
        }
      }
      default -> log.warn("Unknown action {}", name);
    }
  }

  @Override
  public void update(final float tpf) {
    // Flatten the camera axes so looking up or down doesn't push the character into the air
    final Camera cam = getApplication().getCamera();
    cam.getDirection(camForward).setY(0).normalizeLocal();
    cam.getLeft(camLeft).setY(0).normalizeLocal();

    walkDirection.zero();
    if (forward) {
      walkDirection.addLocal(camForward);
    }
    if (backward) {
      walkDirection.subtractLocal(camForward);
    }
    if (left) {
      walkDirection.addLocal(camLeft);
    }
    if (right) {
      walkDirection.subtractLocal(camLeft);
    }

    final boolean moving = walkDirection.lengthSquared() > 0;
    if (moving) {
      walkDirection.normalizeLocal();
      character.setViewDirection(walkDirection);
    }
    // Units per second; physics integrates it, so no tpf here
    character.setWalkDirection(walkDirection.multLocal(MOVE_SPEED));

    play(animationFor(character.isOnGround(), moving));
  }

  /* default */ Spatial getPlayer() {
    return player;
  }

  /* default */ String getCurrentAnimation() {
    return composer == null
        ? null : composer.getLayer(AnimComposer.DEFAULT_LAYER).getCurrentActionName();
  }

  private void play(final String animation) {
    if (composer == null || animation.equals(getCurrentAnimation())) {
      return;
    }
    if (composer.getAnimClipsNames().contains(animation)) {
      composer.setCurrentAction(animation);
    } else {
      log.error("Model is missing animation: {}", animation);
    }
  }

  private ChaseCamera createChaseCamera(final Camera cam, final InputManager input) {
    final ChaseCamera camera = new ChaseCamera(cam, player, input);
    camera.setSmoothMotion(true);
    // Start behind the character (it faces +Z), look from slightly above, over the shoulders,
    // and trail behind when the player turns
    camera.setDefaultHorizontalRotation(-FastMath.HALF_PI);
    camera.setDefaultVerticalRotation(0.2f);
    camera.setDefaultDistance(6f);
    camera.setLookAtOffset(new Vector3f(0, 1f, 0));
    camera.setTrailingEnabled(true);
    camera.setChasingSensitivity(5f);
    return camera;
  }

  /**
   * Finds the AnimComposer anywhere in the model's hierarchy.
   */
  private static AnimComposer findComposer(final Spatial spatial) {
    AnimComposer control = spatial.getControl(AnimComposer.class);
    if (control == null && spatial instanceof Node node) {
      control = node.getChildren().stream()
          .map(PlayerAppState::findComposer)
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }
    return control;
  }
}
