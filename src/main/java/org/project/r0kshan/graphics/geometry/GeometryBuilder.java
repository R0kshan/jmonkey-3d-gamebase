package org.project.r0kshan.graphics.geometry;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;


/**
 * Builder for geometry shapes (ex: squares)
 */
public class GeometryBuilder {

    private Geometry geometry;
    private ShapeEnum shapeEnum;

    /**
     * Initializes the GeometryBuilder with a shape and its parameters.
     * @param shapeEnum The type of shape to create
     * @param x For BOX: width, For SPHERE: radial samples
     * @param y For BOX: height, For SPHERE: z samples
     * @param z For BOX: depth, For SPHERE: radius
     */
    public GeometryBuilder init(ShapeEnum shapeEnum, float x, float y, float z) {
        if (shapeEnum == null) {
            throw new IllegalArgumentException("ShapeEnum cannot be null");
        }

        this.shapeEnum = shapeEnum;
        this.geometry = createGeometry(shapeEnum, x, y, z);
        return this;
    }

    /**
     * Factory method to create the appropriate geometry based on shape type.
     */
    private Geometry createGeometry(ShapeEnum shapeEnum, float x, float y, float z) {
        return switch (shapeEnum) {
            case BOX -> new Geometry(shapeEnum.getName(), new Box(x, y, z));
            case SPHERE -> {
                Sphere sphereMesh = new Sphere(Math.round(x), Math.round(y), z);
                sphereMesh.setTextureMode(Sphere.TextureMode.Projected);
                TangentBinormalGenerator.generate(sphereMesh);
                yield new Geometry(shapeEnum.getName(), sphereMesh);
            }
        };
    }

    /**
     * Sets the local translation of the geometry.
     */
    public GeometryBuilder setLocalTranslation(float x, float y, float z) {
        ensureBuilderIsInitialized();
        this.geometry.setLocalTranslation(new Vector3f(x, y, z));
        return this;
    }

    /**
     * Sets the material for this geometry.
     */
    public GeometryBuilder setMaterial(Material material) {
        ensureBuilderIsInitialized();
        if (material == null) {
            throw new IllegalArgumentException("Material cannot be null");
        }
        this.geometry.setMaterial(material);
        return this;
    }

    /**
     * Sets texture mode to projected (only applicable for spheres).
     * This method is automatically called for spheres during initialization.
     */
    public GeometryBuilder setTextureModeProjected() {
        // This is now handled automatically in createGeometry for spheres
        return this;
    }

    /**
     * Rotates the geometry by the specified angles.
     */
    public GeometryBuilder setRotation(float x, float y, float z) {
        ensureBuilderIsInitialized();
        this.geometry.rotate(x, y, z);
        return this;
    }

    /**
     * Builds and returns the configured Geometry.
     */
    public Geometry build() {
        ensureBuilderIsInitialized();
        return this.geometry;
    }

    /**
     * Ensures that the builder has been properly initialized.
     * @throws IllegalStateException if the builder has not been initialized
     */
    private void ensureBuilderIsInitialized() {
        if (this.geometry == null) {
            throw new IllegalStateException("GeometryBuilder must be initialized with init() before use");
        }
    }


}
