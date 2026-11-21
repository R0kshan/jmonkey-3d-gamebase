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
}
