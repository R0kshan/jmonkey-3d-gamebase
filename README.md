# A game base for a 3D game using JMonkey

## 🟢 Status: In Development

## Context
I started this project to learn and experiment with game development using [JMonkey Engine](https://jmonkeyengine.org/). 

## Run

Run `org.project.r0kshan.Gamebase`. A settings dialog (resolution, fullscreen, vsync, anti-aliasing) opens first and remembers your choices.
On macOS, do not pass `-XstartOnFirstThread`: the game no longer needs it, and with it the settings dialog is skipped.

## Objective

My goal for the initial development of this 3D game base is to implement the following features: 
- Physics
- Controls and animation for a humanoid character able to interact with the environment
- A basic settings menu

> More features may be added later on, such as a randomized landscape.

### Software Design Objectives 

- A codebase that adheres to software design principles, as well as JMonkey Engine philosophy and best practices
- Maintainability & Clean Code
- Performance testing & profiling (in game: F6 toggles the detailed profiler, F5 the stats overlay)
