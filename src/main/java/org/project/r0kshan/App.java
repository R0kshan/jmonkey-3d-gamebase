package org.project.r0kshan;

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

    public App() {
        super(new StatsAppState(), new FlyCamAppState(), new AudioListenerState(), new DebugKeysAppState());
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();

    }

    @Override
    public void simpleInitApp() {

        Path classesDir = null;
        try {
            classesDir = Paths.get(
                    App.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Path moduleRoot = classesDir.getParent().getParent();

        assetManager.registerLocator(moduleRoot.resolve("src/main/resources/assets").toString(), FileLocator.class);
        assetManager.registerLocator(moduleRoot.resolve("src/main/resources/assets").toString(), ClasspathLocator.class);

        stoneGroundTexture();

        //helloMaterial();
    }

    public void stoneGroundTexture() {

        // Material loaded but shows nothing
        /*Material sphereMat = new MaterialBuilder()
                .init(assetManager,"MatDefs/CustomMatDef.j3md")
                .useMaterialColors()
                .setSpecularColor(ColorRGBA.White)
                .setDiffuseColor(ColorRGBA.White)
                .setShininess(64f)
                .build();*/

        // Works (shows texture)
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        sphereMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        sphereMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
        sphereMat.setBoolean("UseMaterialColors", true);
        sphereMat.setColor("Diffuse", ColorRGBA.White);
        sphereMat.setColor("Specular", ColorRGBA.White);
        sphereMat.setFloat("Shininess", 64f);  // [0,128]

        Geometry sphereGeo = new GeometryBuilder()
                .init(ShapeEnum.SPHERE, 32, 32, 2f)
                .setLocalTranslation(0, 2, -2)
                .setRotation(1.6f, 0, 0)
                .setMateriel(sphereMat)
                .build();

        rootNode.attachChild(sphereGeo);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

    public void helloWorld() {
        // Create a bluebox at coordinates (1,-1,1)
        Material mat1 = new MaterialBuilder()
                .init(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
                .addColor("Color", ColorRGBA.Blue)
                .build();

        Geometry blueBox =
                new GeometryBuilder()
                        .init(ShapeEnum.BOX, 1, 1, 1)
                        .setLocalTranslation(1, -1, 1)
                        .setMateriel(mat1)
                        .build();

        // create a red box straight above the blue one at (1,3,1)
        Material mat2 = new MaterialBuilder()
                .init(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
                .addColor("Color", ColorRGBA.Red)
                .build();

        Geometry redBox =
                new GeometryBuilder()
                        .init(ShapeEnum.BOX, 1, 1, 1)
                        .setLocalTranslation(1, 3, 1)
                        .setMateriel(mat2)
                        .build();

        // Create a pivot node at (0,0,0) and attach it to the root node
        Node pivot = new Node("pivot");
        rootNode.attachChild(pivot); // put this node in the scene

        // Attach the two boxes to the *pivot* node. (And transitively to the root node.)
        pivot.attachChild(blueBox);
        pivot.attachChild(redBox);

        // Rotate the pivot node: Note that both boxes have rotated!
        pivot.rotate(.4f, .4f, 0f);
    }

    public void helloMaterial() {
        /** A simple textured cube -- in good MIP map quality. */
        Box cube1Mesh = new Box(1f, 1f, 1f);
        Geometry cube1Geo = new Geometry("My Textured Box", cube1Mesh);
        cube1Geo.setLocalTranslation(new Vector3f(-3f, 1.1f, 0f));
        Material cube1Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture cube1Tex = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        cube1Mat.setTexture("ColorMap", cube1Tex);
        cube1Geo.setMaterial(cube1Mat);
        rootNode.attachChild(cube1Geo);

        /** A translucent/transparent texture, similar to a window frame. */
        Box cube2Mesh = new Box(1f, 1f, 0.01f);
        Geometry cube2Geo = new Geometry("window frame", cube2Mesh);
        Material cube2Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        cube2Mat.setTexture("ColorMap", assetManager.loadTexture("Textures/ColoredTex/Monkey.png"));
        cube2Mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);  // activate transparency
        cube2Geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        cube2Geo.setMaterial(cube2Mat);
        rootNode.attachChild(cube2Geo);

        /* A bumpy rock with a shiny light effect. To make bumpy objects you must create a NormalMap. */


        Material sphereMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        sphereMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        sphereMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
        sphereMat.setBoolean("UseMaterialColors", true);
        sphereMat.setColor("Diffuse", ColorRGBA.White);
        sphereMat.setColor("Specular", ColorRGBA.White);
        sphereMat.setFloat("Shininess", 64f);  // [0,128]

        Sphere sphereMesh = new Sphere(32, 32, 2f);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);

        TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect

        sphereGeo.setMaterial(sphereMat);
        //sphereGeo.setMaterial((Material) assetManager.loadMaterial("Materials/MyCustomMaterial.j3m"));
        sphereGeo.setLocalTranslation(0, 2, -2); // Move it a bit
        sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit


        rootNode.attachChild(sphereGeo);

        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }

}
