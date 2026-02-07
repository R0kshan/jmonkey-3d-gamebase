package org.project.r0kshan.graphics.materials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

/**
 * Builder for materials (ex: stone, wood ...)
 */
public class MaterialBuilder {

    private final String USE_MATERIAL_COLORS = "UseMaterialColors";
    private final String DIFFUSE_COLOR = "Diffuse";
    private final String SPECULAR_COLOR = "Specular";

    private final String SHININESS = "Shininess";

    private Material material;
    private AssetManager assetManager;

    public MaterialBuilder init(AssetManager assetManager, String defName) {
        this.assetManager = assetManager;
        this.material = new Material(assetManager,defName);
        return this;
    }

    public MaterialBuilder addColor(String color, ColorRGBA colorRGBA) {
        this.material.setColor(color,colorRGBA);
        return this;
    }

    public MaterialBuilder setTexture(String textureName, String texturePath) {
        Texture texture = this.assetManager.loadTexture(texturePath);
        this.material.setTexture(textureName,texture);
        return this;
    }

    public MaterialBuilder setTexture(AssetManager assetManager, String textureName, String texturePath) {
        this.material.setTexture(textureName, assetManager.loadTexture(texturePath));
        return this;
    }

    public MaterialBuilder setBlendMode(RenderState.BlendMode blendMode) {
        this.material.getAdditionalRenderState().setBlendMode(blendMode);
        return this;
    }

    public MaterialBuilder useMaterialColors() {
        this.material.setBoolean(USE_MATERIAL_COLORS,true);
        return this;
    }

    public MaterialBuilder setDiffuseColor(ColorRGBA color) {
        this.material.setColor(DIFFUSE_COLOR,color);
        return this;
    }

    public MaterialBuilder setSpecularColor(ColorRGBA color) {
        this.material.setColor(SPECULAR_COLOR,color);
        return this;
    }

    public MaterialBuilder setShininess(float shininess) {
        this.material.setFloat(SHININESS,shininess);
        return this;
    }

    public Material build() {
        return this.material;
    }
}
