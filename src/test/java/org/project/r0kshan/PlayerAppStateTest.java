package org.project.r0kshan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerAppStateTest {

  private static final long WAIT_MILLIS = 5_000;

  private HeadlessApp app;
  private PlayerAppState player;

  @BeforeEach
  void start() throws Exception {
    player = new PlayerAppState();
    app = HeadlessApp.start(BulletAppState::new, WorldAppState::new, () -> player);
    // Let the character land on the ground before each scenario
    assertTrue(waitFor(() -> PlayerAppState.IDLE_ANIM.equals(animation())));
  }

  @AfterEach
  void stop() {
    app.stop(true);
  }

  @Test
  void animationFollowsPhysicsState() {
    assertEquals(PlayerAppState.IDLE_ANIM, PlayerAppState.animationFor(true, false));
    assertEquals(PlayerAppState.RUN_ANIM, PlayerAppState.animationFor(true, true));
    assertEquals(PlayerAppState.JUMP_ANIM, PlayerAppState.animationFor(false, false));
    assertEquals(PlayerAppState.JUMP_ANIM, PlayerAppState.animationFor(false, true));
  }

  @Test
  void walkingMovesTheCharacterAndPlaysRun() throws Exception {
    final Vector3f origin = position();

    press(PlayerAppState.FORWARD, true);
    assertTrue(waitFor(() -> horizontalDistance(origin, position()) > 1f));
    assertEquals(PlayerAppState.RUN_ANIM, animation());

    press(PlayerAppState.FORWARD, false);
    assertTrue(waitFor(() -> PlayerAppState.IDLE_ANIM.equals(animation())));
  }

  @Test
  void eachDirectionMovesTheCharacter() throws Exception {
    for (final String action : new String[] {
        PlayerAppState.BACKWARD, PlayerAppState.LEFT, PlayerAppState.RIGHT}) {
      final Vector3f origin = position();
      press(action, true);
      assertTrue(waitFor(() -> horizontalDistance(origin, position()) > 1f), action);
      press(action, false);
    }
  }

  @Test
  void jumpLeavesTheGroundAndLands() throws Exception {
    press(PlayerAppState.JUMP, true);
    assertTrue(waitFor(() -> PlayerAppState.JUMP_ANIM.equals(animation())));
    assertTrue(waitFor(() -> position().y > 0.1f));
    press(PlayerAppState.JUMP, false);

    assertTrue(waitFor(() -> PlayerAppState.IDLE_ANIM.equals(animation())));
  }

  @Test
  void unknownActionIsIgnored() throws Exception {
    press("Unknown", true);
    assertEquals(PlayerAppState.IDLE_ANIM, animation());
  }

  @Test
  void disableReleasesInputAndScene() throws Exception {
    press(PlayerAppState.FORWARD, true);
    app.run(() -> player.setEnabled(false));

    assertFalse(app.call(() -> app.getInputManager().hasMapping(PlayerAppState.FORWARD)));
    assertTrue(app.call(() -> player.getPlayer().getParent() == null));

    // Re-enabling must not resume the walk that was held when disabled
    app.run(() -> player.setEnabled(true));
    final Vector3f origin = position();
    Thread.sleep(300);
    assertTrue(horizontalDistance(origin, position()) < 0.1f);
    assertTrue(app.call(() -> app.getInputManager().hasMapping(PlayerAppState.FORWARD)));
  }

  @Test
  void detachRemovesCharacterFromPhysics() throws Exception {
    final BulletAppState bullet = app.call(() -> app.getStateManager()
        .getState(BulletAppState.class));
    final int before = app.call(() -> bullet.getPhysicsSpace().countRigidBodies());

    app.call(() -> app.getStateManager().detach(player));
    app.run(() -> { });

    assertEquals(before - 1, (int) app.call(() -> bullet.getPhysicsSpace().countRigidBodies()));
  }

  private void press(final String action, final boolean pressed) throws Exception {
    app.run(() -> player.onAction(action, pressed, 0));
  }

  private String animation() throws Exception {
    // Read on the render thread, like everything else touching the scene
    return app.call(() -> String.valueOf(player.getCurrentAnimation()));
  }

  private Vector3f position() throws Exception {
    return app.call(() -> player.getPlayer().getWorldTranslation().clone());
  }

  private static float horizontalDistance(final Vector3f a, final Vector3f b) {
    return new Vector3f(a.x - b.x, 0, a.z - b.z).length();
  }

  private static boolean waitFor(final Callable<Boolean> condition) throws Exception {
    final long deadline = System.currentTimeMillis() + WAIT_MILLIS;
    while (System.currentTimeMillis() < deadline) {
      if (condition.call()) {
        return true;
      }
      Thread.sleep(20);
    }
    return false;
  }
}
