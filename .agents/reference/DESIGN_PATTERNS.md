Design patterns are proven solutions to recurring design problems. Use one only when the problem it solves is actually in front of you: a pattern added "for later" is needless complexity (see CLEAN_CODE.md). Prefer the simplest code that works, and reach for a pattern when that code starts to repeat itself, branch on type, or resist change.
_____________________________________

## Before using a pattern
1. Name the problem first. If you cannot say which problem the pattern solves here, do not use it.
2. Check jMonkeyEngine first. The engine already implements several patterns (see the last section); extend those rather than building a parallel version.
3. Start without it. Refactor toward a pattern when a second or third case appears, not for the first.
4. Name classes after the pattern role when it helps a reader (`...Factory`, `...Adapter`, `...State`).

## Creational patterns: how objects are created
1. Factory Method. Let subclasses or a method decide which concrete class to create. Use when the caller should not depend on the concrete type.
2. Abstract Factory. Create families of related objects without naming their classes. Use when several objects must always match each other (for example, one visual theme).
3. Builder. Build a complex object step by step. Use when a constructor would need many parameters or many optional ones.
4. Prototype. Create new objects by copying an existing one. Use when copying is cheaper or simpler than building from scratch.
5. Singleton. Guarantee one instance with a global access point. Avoid by default: it hides dependencies and makes testing harder. Prefer passing the instance in (dependency injection).

## Structural patterns: how objects are composed
1. Adapter. Wrap an object so it matches the interface the caller expects. Use when integrating a library or legacy code you cannot change.
2. Bridge. Split an abstraction from its implementation so both can vary independently. Use when two dimensions of variation would otherwise multiply subclasses.
3. Composite. Treat a tree of objects and a single object the same way. Use for part-whole hierarchies.
4. Decorator. Add behaviour by wrapping an object that has the same interface. Use instead of subclassing when behaviours must be combined freely.
5. Facade. Offer one simple interface over a complicated subsystem. Use to hide setup or wiring that callers should not need to know.
6. Flyweight. Share common state between many small objects to save memory. Use only when a profiler shows memory pressure from many similar objects.
7. Proxy. Stand in for another object to control access to it (lazy loading, caching, access checks). Use when the real object is expensive or must be guarded.

## Behavioral patterns: how objects communicate
1. Chain of Responsibility. Pass a request along a chain of handlers until one handles it. Use when several handlers may apply and the order matters.
2. Command. Turn a request into an object. Use for input actions, undo/redo, queuing or replaying actions.
3. Iterator. Walk through a collection without exposing its structure. In Java, implement `Iterable` rather than writing a custom iterator API.
4. Mediator. Make objects talk through one coordinator instead of to each other. Use when many objects reference each other directly.
5. Memento. Capture and restore an object's state without exposing its internals. Use for save games, checkpoints or undo.
6. Observer. Notify subscribers when something changes. Use when several parts must react to an event without the source knowing them.
7. State. Let an object change behaviour when its internal state changes. Use instead of growing if/else or switch on a state flag (idle, running, jumping).
8. Strategy. Make a family of interchangeable algorithms. Use when the same task has several ways to be done and the choice changes at runtime.
9. Template Method. Define an algorithm's skeleton in a base class and let subclasses fill in steps. Use when variants share most of their steps.
10. Visitor. Add operations to a set of classes without changing them. Use rarely: only when a stable class hierarchy needs many unrelated operations.

## Patterns jMonkeyEngine already provides
1. Composite: the scene graph. `Node` and `Geometry` are both `Spatial`.
2. State: `AppState` for game modes and systems that can be attached, enabled and detached.
3. Observer: `ActionListener`, `AnalogListener` and other engine listeners.
4. Command: input mappings in `InputManager`, which turn triggers into named actions.
5. Components: `Control` classes attached to a `Spatial` add behaviour without subclassing it. Not a pattern from this catalogue, but prefer it to subclassing `Spatial`.
6. Prototype: `Spatial.clone()` to copy loaded models.

--

Pattern catalogue based on [Refactoring.Guru: Design Patterns in Java](https://refactoring.guru/design-patterns/java). Descriptions are summaries written for this project.
