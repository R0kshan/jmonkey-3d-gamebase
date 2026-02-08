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
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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
    private final float MOVE_SPEED = 20f;
    private Spatial player;
    private AnimComposer composer;
    private boolean left = false, right = false, up = false, down = false;
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (isPressed) System.out.println("Key Pressed: " + name);

            if (name.equals("Left")) left = isPressed;
            if (name.equals("Right")) right = isPressed;
            if (name.equals("Up")) up = isPressed;
            if (name.equals("Down")) down = isPressed;

            // Update animation state
            updateAnimation();
        }
    };

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

        initKeys();

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
        player = assetManager.loadModel("Models/Humanoid/BasicHumanoid.glb");
        player.setLocalScale(1.0f);
        player.setLocalTranslation(0, 5, 0);
        //player.rotate(0, FastMath.PI, 0);
        player.rotate(0, -10f, 0);

        // Use the helper method to search the whole model tree
        composer = findComposer(player);

        if (composer != null) {
            System.out.println("Success! Found composer on: " + composer.getSpatial().getName());
            System.out.println("Available animations: " + composer.getAnimClipsNames());

            // Ensure "Idle" exists before playing
            if (composer.getAnimClipsNames().contains("idle")) {
                composer.setCurrentAction("idle");
            } else if (!composer.getAnimClipsNames().isEmpty()) {
                // Fallback: play the first animation found
                String firstAnim = composer.getAnimClipsNames().iterator().next();
                composer.setCurrentAction(firstAnim);
            }
        } else {
            System.err.println("Could not find AnimComposer .glb!");
        }

        rootNode.attachChild(player);
    }

    /**
     * Helper to find AnimComposer anywhere in the model's hierarchy
     */
    private AnimComposer findComposer(Spatial s) {
        // Check if the current spatial has the control
        AnimComposer control = s.getControl(AnimComposer.class);
        if (control != null) return control;

        // If it's a Node, check all of its children
        if (s instanceof Node) {
            for (Spatial child : ((Node) s).getChildren()) {
                AnimComposer result = findComposer(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    public void setupChaseCamera() {
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setSmoothMotion(true);

        // 1. Horizontal Rotation:
        // If you see the front now, change FastMath.PI to 0.
        // If you see the side, try FastMath.HALF_PI (90 degrees).
        //chaseCam.setDefaultHorizontalRotation(FastMath.PI);
        chaseCam.setDefaultHorizontalRotation(0);

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

    private void initKeys() {
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W)); // Physical Z on AZERTY
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A)); // Physical Q on AZERTY
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S)); // Physical S
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D)); // Physical D

        inputManager.addListener(actionListener, "Left", "Right", "Up", "Down");
    }

    private void updateAnimation() {

        System.out.println("Composer : " + composer);
        if (composer == null) return;

        System.out.println("Determine animation");

        // Determine which animation we WANT to play
        String desiredAnim = (up || down || left || right) ? "run" : "idle";

        // Only change if it's different from the CURRENTLY playing animation
        if (composer.getCurrentAction() == null ||
                !composer.getCurrentAction().toString().equals(desiredAnim)) {

            // Safety check: verify the model actually has this animation
            if (composer.getAnimClipsNames().contains(desiredAnim)) {
                composer.setCurrentAction(desiredAnim);
                System.out.println("Currenta animation: " + desiredAnim);
            } else {
                System.out.println("Warning: Model is missing animation: " + desiredAnim);
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // 1. Get the camera direction, but ignore the Y (up/down) axis
        // so the character doesn't fly into the air when looking up.
        Vector3f camDir = cam.getDirection().clone().setY(0).normalizeLocal();
        Vector3f camLeft = cam.getLeft().clone().setY(0).normalizeLocal();
        Vector3f walkDirection = new Vector3f(0, 0, 0);

        // 2. Calculate direction based on which keys are held
        if (up) walkDirection.addLocal(camDir);
        if (down) walkDirection.subtractLocal(camDir);
        if (left) walkDirection.addLocal(camLeft);
        if (right) walkDirection.subtractLocal(camLeft);

        // 3. If any key is pressed, move and rotate
        if (walkDirection.length() > 0) {
            walkDirection.normalizeLocal();

            // Move the player spatial
            player.move(walkDirection.mult(MOVE_SPEED * tpf));

            // Make the player face the direction they are walking
            // We use slerp for smooth rotation (optional, but looks better)
            com.jme3.math.Quaternion lookRotation = new com.jme3.math.Quaternion();
            lookRotation.lookAt(walkDirection, Vector3f.UNIT_Y);
            // player.getLocalRotation().slerp(lookRotation, 0.2f);
            player.getLocalRotation().slerp(lookRotation, 10f * tpf);
        }
    }


}
