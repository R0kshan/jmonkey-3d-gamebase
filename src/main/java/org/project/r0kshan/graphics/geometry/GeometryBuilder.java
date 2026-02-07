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

    public GeometryBuilder init(ShapeEnum shapeEnum, float x, float y, float z) {
        this.shapeEnum = shapeEnum;

        switch (shapeEnum) {
            case ShapeEnum.BOX -> this.geometry = new Geometry(shapeEnum.getName(), new Box(x, y, z));
            case ShapeEnum.SPHERE -> {
                Sphere sphereMesh = new Sphere(Math.round(x), Math.round(y), z);
                sphereMesh.setTextureMode(Sphere.TextureMode.Projected);
                TangentBinormalGenerator.generate(sphereMesh);
                this.geometry = new Geometry(shapeEnum.getName(), sphereMesh);

            }
        }
        return this;
    }

    public GeometryBuilder setLocalTranslation(float x, float y, float z) {
        this.geometry.setLocalTranslation(new Vector3f(x, y, z));
        return this;
    }

    public GeometryBuilder setMateriel(Material material) {
        this.geometry.setMaterial(material);
        return this;
    }

    public GeometryBuilder setTextureModeProjected() {
        return this;
    }

    public GeometryBuilder setRotation(float x, float y, float z) {
        this.geometry.rotate(x, y, z);
        return this;
    }

    public Geometry build() {
        return this.geometry;
    }


}
