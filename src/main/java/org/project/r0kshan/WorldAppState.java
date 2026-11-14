package org.project.r0kshan;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

/**
 * Static environment: lighting, a grass ground with a matching collision plane, and a few
 * dynamic crates. Requires a {@link BulletAppState} to be attached first.
 */
public class WorldAppState extends BaseAppState {

  /* default */ static final String CRATE_NAME = "Crate";
  /* default */ static final float CRATE_HALF_SIZE = 0.5f;
  private static final float CRATE_MASS = 10f;
  private static final float GROUND_SIZE = 100f;

  private final Node worldNode = new Node("World");
  private final DirectionalLight sun = new DirectionalLight(
      new Vector3f(-0.5f, -1.0f, -0.5f).normalizeLocal(), ColorRGBA.White.mult(1.2f));
  private final AmbientLight ambient = new AmbientLight(ColorRGBA.White.mult(0.3f));
  private PhysicsSpace physicsSpace;
  private PhysicsRigidBody floor;

  @Override
  protected void initialize(final Application app) {
    physicsSpace = getState(BulletAppState.class, true).getPhysicsSpace();
    worldNode.attachChild(createGround(app.getAssetManager()));

    // Infinite static plane at y=0 matching the visual ground
    floor = new PhysicsRigidBody(
        new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, 0)), PhysicsBody.massForStatic);
    physicsSpace.addCollisionObject(floor);

    createCrates(app.getAssetManager());
  }

  @Override
  protected void onEnable() {
    final Node rootNode = ((SimpleApplication) getApplication()).getRootNode();
    // Lights go on the root so they also light the character
    rootNode.addLight(sun);
    rootNode.addLight(ambient);
    rootNode.attachChild(worldNode);
  }

  @Override
  protected void onDisable() {
    final Node rootNode = ((SimpleApplication) getApplication()).getRootNode();
    rootNode.removeLight(sun);
    rootNode.removeLight(ambient);
    worldNode.removeFromParent();
  }

  @Override
  protected void cleanup(final Application app) {
    physicsSpace.removeAll(worldNode);
    physicsSpace.removeCollisionObject(floor);
  }

  /* default */ Node getWorldNode() {
    return worldNode;
  }

  private Geometry createGround(final AssetManager assetManager) {
    final Quad groundQuad = new Quad(GROUND_SIZE, GROUND_SIZE);
    final Geometry groundGeom = new Geometry("GrassGround", groundQuad);
    final Material grassMat = assetManager.loadMaterial("Materials/GrassGround.j3m");

    // Without tiling one blade of grass would stretch across the whole floor
    grassMat.getTextureParam("DiffuseMap").getTextureValue().setWrap(Texture.WrapMode.Repeat);
    groundQuad.scaleTextureCoordinates(new Vector2f(10f, 10f));
    groundGeom.setMaterial(grassMat);

    // Quads are vertical by default: lay it flat, then center it on the origin
    groundGeom.rotate(-FastMath.HALF_PI, 0, 0);
    groundGeom.setLocalTranslation(-GROUND_SIZE / 2, 0, GROUND_SIZE / 2);
    return groundGeom;
  }

  private void createCrates(final AssetManager assetManager) {
    final Box crateMesh = new Box(CRATE_HALF_SIZE, CRATE_HALF_SIZE, CRATE_HALF_SIZE);
    final Material crateMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    crateMat.setBoolean("UseMaterialColors", true);
    crateMat.setColor("Diffuse", ColorRGBA.Brown);
    crateMat.setColor("Ambient", ColorRGBA.Brown);

    final Vector3f[] positions = {
        new Vector3f(3, 2, -3), new Vector3f(-3, 3, -4), new Vector3f(0, 4, -6)
    };
    for (final Vector3f position : positions) {
      final Geometry crate = new Geometry(CRATE_NAME, crateMesh);
      crate.setMaterial(crateMat);
      crate.setLocalTranslation(position);
      crate.addControl(new RigidBodyControl(CRATE_MASS));
      worldNode.attachChild(crate);
    }
    physicsSpace.addAll(worldNode);
  }
}
