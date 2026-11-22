package org.project.r0kshan;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import junit.framework.TestCase;

/**
 * Checks the camera-behind angle, whose signs are easy to get wrong.
 */
public class GamebaseTest extends TestCase {

  private static final float EPSILON = 1e-4f;

  /** ChaseCamera puts the camera at (cos a, sin a) around the target on the XZ plane. */
  private static Vector3f cameraOffset(final float angle) {
    return new Vector3f(FastMath.cos(angle), 0, FastMath.sin(angle));
  }

  public void testCameraEndsUpOppositeTheFacing() {
    final Vector3f[] facings = {
        Vector3f.UNIT_Z, Vector3f.UNIT_X, new Vector3f(-1, 0, -1).normalizeLocal(),
    };
    for (final Vector3f forward : facings) {
      final Vector3f offset = cameraOffset(Gamebase.behindAngle(forward));
      assertTrue("camera behind " + forward,
          offset.add(forward).length() < EPSILON);
    }
  }

  public void testTurnTowardStepsTheShortWayAndStops() {
    // Small gap: reaches the target exactly, no lag
    assertEquals(0.5f, Gamebase.turnToward(0.4f, 0.5f, 1f), EPSILON);
    // Large gap: capped at maxStep
    assertEquals(0.3f, Gamebase.turnToward(0f, 2f, 0.3f), EPSILON);
    // Across the -PI/PI seam it turns the short way (down from 3.0 past PI), not back through 0
    final float stepped = Gamebase.turnToward(3.0f, -3.0f, 0.1f);
    assertEquals(3.1f, stepped, EPSILON);
  }
}
