package org.project.r0kshan;

import com.jme3.anim.AnimComposer;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioListenerState;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends SimpleApplication {


    // Asset paths
    private static final String ASSETS_PATH = "src/main/resources/assets";

    private Spatial player; // Move this here
    private AnimComposer composer;

    public App() {
        super(new StatsAppState(), new FlyCamAppState(), new AudioListenerState(), new DebugKeysAppState());
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();

    }

    @Override
    public void simpleInitApp() {


        Path classesDir;
        try {
            classesDir = Paths.get(
                    App.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Path moduleRoot = classesDir.getParent().getParent();

        String assetsPath = moduleRoot.resolve(ASSETS_PATH).toString();
        assetManager.registerLocator(assetsPath, FileLocator.class);
        assetManager.registerLocator(assetsPath, ClasspathLocator.class);

        setupLighting();
        createGrassGround();
        createCharacter();
        setupChaseCamera();

        //cam.setLocation(new Vector3f(0, 10, 20)); // Move up and back
        //cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y); // Look at the center

    }

    /**
     * Sets up basic scene lighting
     * Necessary to avoid black screen
     */
    public void setupLighting() {
        // Add directional light (like sun)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1.0f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(1.2f));  // Bright white light
        rootNode.addLight(sun);

        // Add ambient light for overall brightness
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.3f));  // Subtle ambient
        rootNode.addLight(ambient);
    }

    /**
     * Creates a large grass ground plane
     */
    public void createGrassGround() {

        // Create the Geometry for a grass terrain of 100x100 units
        Quad groundQuad = new Quad(100f, 100f);
        Geometry groundGeom = new Geometry("GrassGround", groundQuad);

        // Load the Material instance (.j3m)
        Material grassMat = (Material) assetManager.loadMaterial("Materials/GrassGround.j3m");

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

    public void createCharacter() {
        // 1. Load the model
        // Note: glb files are loaded as Spatials (which can be Nodes or Geometries)
        player = assetManager.loadModel("Models/Humanoid/TeslaBot.glb");
        player.setLocalScale(1.0f); // Adjust scale as needed
        player.setLocalTranslation(0, 0, 0); // Place him on the grass

        // ADD THIS LINE: Rotate the model itself 180 degrees
        player.rotate(0, FastMath.PI, 0);

        // 2. Find the Animation Composer
        // We search the model's children because the composer is often on a sub-node
        composer = player.getControl(AnimComposer.class);
        if (composer == null) {
            // Sometimes the control is on the child node (the armature)
            composer = ((Node) player).getChild(0).getControl(AnimComposer.class);
        }

        if (composer != null) {
            // 3. List available animations (helpful for debugging)
            System.out.println("Available animations: " + composer.getAnimClipsNames());

            // 4. Run an animation (replace "Idle" with your actual clip name)
            composer.setCurrentAction("Idle");
        }

        rootNode.attachChild(player);
    }

    public void setupChaseCamera() {
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setSmoothMotion(true);

        // 1. Horizontal Rotation:
        // If you see the front now, change FastMath.PI to 0.
        // If you see the side, try FastMath.HALF_PI (90 degrees).
        chaseCam.setDefaultHorizontalRotation(FastMath.PI);

        // 2. Vertical Rotation:
        // This tilts the camera down so you are looking from slightly above.
        // 0.3f to 0.5f is usually a good "over the shoulder" angle.
        chaseCam.setDefaultVerticalRotation(0.2f);

        // 3. Distance and Offsets
        chaseCam.setDefaultDistance(6f);
        chaseCam.setLookAtOffset(new Vector3f(0, 1f, 0)); // Look at head/shoulders

        // 4. Trailing:
        // This ensures the camera stays behind the player when they turn.
        chaseCam.setTrailingEnabled(true);
        chaseCam.setChasingSensitivity(5f);
    }


}
