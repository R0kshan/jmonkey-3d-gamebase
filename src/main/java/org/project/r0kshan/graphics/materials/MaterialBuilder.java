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

    private static final String USE_MATERIAL_COLORS = "UseMaterialColors";
    private static final String DIFFUSE_COLOR = "Diffuse";
    private static final String SPECULAR_COLOR = "Specular";
    private static final String SHININESS = "Shininess";

    private Material material;
    private AssetManager assetManager;

    /**
     * Initializes the MaterialBuilder with an AssetManager and material definition.
     * Must be called before any other methods.
     */
    public MaterialBuilder init(AssetManager assetManager, String defName) {
        if (assetManager == null) {
            throw new IllegalArgumentException("AssetManager cannot be null");
        }
        if (defName == null || defName.trim().isEmpty()) {
            throw new IllegalArgumentException("Material definition name cannot be null or empty");
        }

        this.assetManager = assetManager;
        this.material = new Material(assetManager, defName);
        return this;
    }

    /**
     * Adds a color parameter to the material.
     */
    public MaterialBuilder addColor(String color, ColorRGBA colorRGBA) {
        ensureBuilderIsInitialized();
        if (color == null || colorRGBA == null) {
            throw new IllegalArgumentException("Color name and ColorRGBA cannot be null");
        }
        this.material.setColor(color, colorRGBA);
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

    /**
     * Builds and returns the configured Material.
     */
    public Material build() {
        ensureBuilderIsInitialized();
        return this.material;
    }

    /**
     * Validates that the builder has been properly initialized.
     */
    private void ensureBuilderIsInitialized() {
        if (this.material == null) {
            throw new IllegalStateException("MaterialBuilder must be initialized with init() before use");
        }
    }
}
