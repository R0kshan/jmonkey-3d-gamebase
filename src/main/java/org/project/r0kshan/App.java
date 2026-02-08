package org.project.r0kshan;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.scene.shape.Quad;
import org.project.r0kshan.graphics.geometry.GeometryBuilder;
import org.project.r0kshan.graphics.geometry.ShapeEnum;
import org.project.r0kshan.graphics.materials.MaterialBuilder;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.audio.AudioListenerState;
import com.jme3.light.DirectionalLight;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App extends SimpleApplication {


    // Asset paths
    private static final String ASSETS_PATH = "src/main/resources/assets";

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

        cam.setLocation(new Vector3f(0, 10, 20)); // Move up and back
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y); // Look at the center

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


}
