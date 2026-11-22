Concise guide to the jMonkeyEngine (jME) concepts this project relies on. It is a starting point, not a replacement: when you need exact behaviour, an API detail, or a topic not covered here, read the official documentation linked in each section, or the [JavaDoc](https://javadoc.jmonkeyengine.org). Do not guess engine behaviour; check the docs or the engine sources (the `-sources.jar` in `~/.m2`).

Version note: this project uses jME **3.8.1-stable**. The links point to the 3.9 wiki, which is the current one. A [3.8 wiki](https://wiki.jmonkeyengine.org/docs/3.8/documentation.html) also exists; if a 3.9 page describes something the 3.8.1 API does not have, trust the 3.8 docs and the JavaDoc.
_____________________________________

## Application and game loop
1. `SimpleApplication` gives you `rootNode`, `guiNode`, `cam`, `assetManager`, `inputManager` and `stateManager`. Set up the scene in `simpleInitApp()`, per-frame logic in `simpleUpdate(tpf)`. [SimpleApplication](https://wiki.jmonkeyengine.org/docs/3.9/core/app/simpleapplication.html)
2. Multiply every per-frame change by `tpf` (seconds since the last frame) so speed does not depend on frame rate. [Update Loop](https://wiki.jmonkeyengine.org/docs/3.9/core/app/update_loop.html)
3. `AppSettings` holds resolution, vsync, title and similar options; set it before `start()`. [AppSettings](https://wiki.jmonkeyengine.org/docs/3.9/core/system/appsettings.html)
4. Only touch the scene graph from the render thread. From another thread, use `app.enqueue(...)`. [Multithreading](https://wiki.jmonkeyengine.org/docs/3.9/core/app/multithreading.html)

## Organising game logic
1. `AppState`: a game-wide system or mode (player controls, world, HUD, pause) that can be attached, enabled and detached. Prefer moving logic out of `SimpleApplication` into app states as it grows. [Application States](https://wiki.jmonkeyengine.org/docs/3.9/core/app/state/application_states.html)
2. `Control`: behaviour attached to one `Spatial` (an enemy's AI, a spinning pickup). Prefer a control to subclassing `Spatial`. [Custom Controls](https://wiki.jmonkeyengine.org/docs/3.9/core/scene/control/custom_controls.html)
3. Follow the engine's own advice on structure and performance. [Best Practices](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/concepts/best_practices.html), [Optimization](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/concepts/optimization.html)

## Scene graph
1. Everything visible is a `Spatial`: a `Node` groups children, a `Geometry` holds a mesh and a material. Only things attached under `rootNode` are rendered. [The Scene Graph](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/concepts/the_scene_graph.html), [Spatial](https://wiki.jmonkeyengine.org/docs/3.9/core/scene/spatial.html)
2. Transforms (translation, rotation, scale) are relative to the parent. Use the `set...` methods rather than mutating the returned vector or quaternion, so the engine knows the transform changed.
3. Rotations use `Quaternion`; build them with `fromAngleAxis` or `lookAt`. [3-D Rotation](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/concepts/rotate.html), [Math cheat sheet](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/concepts/math_cheet_sheet.html)
4. Find nodes and controls by walking the tree. [Traverse SceneGraph](https://wiki.jmonkeyengine.org/docs/3.9/core/scene/traverse_scenegraph.html)

## Assets, models and materials
1. Load models, textures and materials through `assetManager` with paths relative to a registered asset root (`src/main/resources/assets` here). [Asset Manager](https://wiki.jmonkeyengine.org/docs/3.9/core/asset/asset_manager.html)
2. glTF (`.glb`) is the model format used here. [3D Models](https://wiki.jmonkeyengine.org/docs/3.9/core/scene/3d_models.html), [Blender glTF export](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/how-to/modeling/blender/blender_gltf.html), [Mixamo animations](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/how-to/modeling/blender/mixamo.html)
3. Materials are defined in `.j3m` files based on `.j3md` definitions. Lit materials show black without a light in the scene. [How to use Materials](https://wiki.jmonkeyengine.org/docs/3.9/core/material/how_to_use_materials.html), [.j3m files](https://wiki.jmonkeyengine.org/docs/3.9/core/material/j3m_material_files.html), [Light and Shadow](https://wiki.jmonkeyengine.org/docs/3.9/core/light/light_and_shadow.html)

## Animation
1. Use `AnimComposer` (the current animation system). `AnimControl` is the old system; do not use it in new code. [Animation with AnimComposer](https://wiki.jmonkeyengine.org/docs/3.9/core/animation/animation_new.html)
2. The composer may sit on a child of the loaded model, not on its root; search the tree for it.

## Input
1. Map triggers (keys, mouse buttons, axes) to named actions with `inputManager.addMapping`, then listen with an `ActionListener` (pressed/released) or `AnalogListener` (continuous values). [Input Handling](https://wiki.jmonkeyengine.org/docs/3.9/core/input/input_handling.html), [Hello Input System](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/beginner/hello_input_system.html)
2. Picking objects with the mouse uses rays and collisions. [Mouse Picking](https://wiki.jmonkeyengine.org/docs/3.9/core/input/mouse_picking.html)

## Camera
1. `cam` is the default camera. `ChaseCamera` orbits and follows a target; `FlyByCamera` is the default free camera and must be disabled when replacing it. [Camera](https://wiki.jmonkeyengine.org/docs/3.9/core/renderer/camera.html), [Follow a Character](https://wiki.jmonkeyengine.org/docs/3.9/core/renderer/making_the_camera_follow_a_character.html)

## Physics (planned)
1. Physics uses Bullet through a `BulletAppState`. A walking character is usually a `BetterCharacterControl`; read the pitfalls page before starting. [Physics](https://wiki.jmonkeyengine.org/docs/3.9/physics/physics.html), [Walking Character](https://wiki.jmonkeyengine.org/docs/3.9/physics/control/walking_character.html), [Bullet Pitfalls](https://wiki.jmonkeyengine.org/docs/3.9/physics/bullet_pitfalls.html), [Hello Physics](https://wiki.jmonkeyengine.org/docs/3.9/tutorials/beginner/hello_physics.html)
2. Collisions without physics (rays, bounding volumes). [Collision and Intersection](https://wiki.jmonkeyengine.org/docs/3.9/core/collision/collision_and_intersection.html)

## Landscapes, effects, GUI (planned)
1. [Terrain](https://wiki.jmonkeyengine.org/docs/3.9/core/terrain/terrain.html), [Sky](https://wiki.jmonkeyengine.org/docs/3.9/core/util/sky.html), [Terrain Collision](https://wiki.jmonkeyengine.org/docs/3.9/core/collision/terrain_collision.html)
2. [Effects overview](https://wiki.jmonkeyengine.org/docs/3.9/core/effect/effects_overview.html), [Particle Emitters](https://wiki.jmonkeyengine.org/docs/3.9/core/effect/particle_emitters.html)
3. [GUI](https://wiki.jmonkeyengine.org/docs/3.9/core/gui/topic_gui.html), [HUD](https://wiki.jmonkeyengine.org/docs/3.9/core/ui/hud.html)

--

Summary written for this project from the [jMonkeyEngine documentation](https://wiki.jmonkeyengine.org/docs/3.9/documentation.html). Help and bug reports: [jMonkeyEngine forum](https://hub.jmonkeyengine.org/).
