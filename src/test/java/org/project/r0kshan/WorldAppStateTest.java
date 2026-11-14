package org.project.r0kshan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jme3.bullet.BulletAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorldAppStateTest {

  private static final float TOLERANCE = 0.05f;

  private HeadlessApp app;
  private WorldAppState world;

  @BeforeEach
  void startWorld() throws Exception {
    world = new WorldAppState();
    app = HeadlessApp.start(BulletAppState::new, () -> world);
  }

  @AfterEach
  void stop() {
    app.stop(true);
  }

  @Test
  void cratesFallAndRestOnTheGround() throws Exception {
    // Crates start between y=2 and y=4; poll until physics settles them on the plane
    final long deadline = System.currentTimeMillis() + 5_000;
    List<Vector3f> crates = crateLocations();
    while (!allResting(crates) && System.currentTimeMillis() < deadline) {
      Thread.sleep(100);
      crates = crateLocations();
    }

    final List<Vector3f> settled = crates;
    assertEquals(3, settled.size());
    assertTrue(allResting(settled), () -> "Crates did not settle: " + settled);
  }

  @Test
  void disableDetachesWorldAndLights() throws Exception {
    final boolean attached = app.call(() -> world.getWorldNode().getParent() != null);
    final int lights = app.call(() -> app.getRootNode().getLocalLightList().size());

    app.run(() -> world.setEnabled(false));

    assertTrue(attached);
    assertEquals(2, lights);
    assertFalse(app.call(() -> world.getWorldNode().getParent() != null));
    assertEquals(0, (int) app.call(() -> app.getRootNode().getLocalLightList().size()));
  }

  @Test
  void detachRemovesPhysicsObjects() throws Exception {
    final BulletAppState bullet = app.call(() -> app.getStateManager()
        .getState(BulletAppState.class));
    final int before = app.call(() -> bullet.getPhysicsSpace().countRigidBodies());

    app.call(() -> app.getStateManager().detach(world));
    // Detached states are cleaned up during the next frame
    app.run(() -> { });

    assertEquals(4, before); // floor + 3 crates
    assertEquals(0, (int) app.call(() -> bullet.getPhysicsSpace().countRigidBodies()));
  }

  private List<Vector3f> crateLocations() throws Exception {
    return app.call(() -> world.getWorldNode().getChildren().stream()
        .filter(child -> WorldAppState.CRATE_NAME.equals(child.getName()))
        .map(Spatial::getWorldTranslation)
        .map(Vector3f::clone)
        .toList());
  }

  private static boolean allResting(final List<Vector3f> crates) {
    return crates.stream()
        .allMatch(c -> Math.abs(c.y - WorldAppState.CRATE_HALF_SIZE) < TOLERANCE);
  }
}
