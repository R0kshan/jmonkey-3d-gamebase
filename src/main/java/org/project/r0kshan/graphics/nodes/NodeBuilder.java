package org.project.r0kshan.graphics.nodes;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Builder for nodes which will be attached to the rootNode. A node will be used for each plane. Each plane will then be attached to the root node
 */
public class NodeBuilder {

    private Node node;

    public NodeBuilder init(String name) {
        this.node = new Node(name);
        return this;
    }

    public NodeBuilder setLocalTranslation(float x, float y, float z) {
        if (node != null) {
            this.node.setLocalTranslation(new Vector3f(x, y, z));
        }
        return this;
    }

    public NodeBuilder setLocalTranslation(Vector3f translation) {
        if (node != null) {
            this.node.setLocalTranslation(translation);
        }
        return this;
    }

    public NodeBuilder rotate(float x, float y, float z) {
        if (node != null) {
            this.node.rotate(x, y, z);
        }
        return this;
    }

    public NodeBuilder attachChild(Spatial child) {
        if (node != null && child != null) {
            this.node.attachChild(child);
        }
        return this;
    }

    public NodeBuilder scale(float scale) {
        if (node != null) {
            this.node.setLocalScale(scale);
        }
        return this;
    }

    public Node build() {
        if (node == null) {
            throw new IllegalStateException("NodeBuilder must be initialized with init() before building");
        }
        return this.node;
    }
}
