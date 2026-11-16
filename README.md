# Obtain Speedrun (Minecraft 1.21.11, Fabric)

Every time you create or join a **singleplayer** world, the mod picks a random
obtainable vanilla item and runs a speedrun for it:

1. **Spin** - a slot-machine style roll through item pictures that slows down and lands.
2. **Reveal** - the chosen item sits in the centre of the screen with "Obtain [ITEM]" for a few seconds.
3. **Timer** - a rainbow timer appears at the top of the screen with the item's picture and name.
4. **Finish** - the moment the item is in your inventory, the timer stops, a celebration card appears, and the run is saved.

Press **H** at any time to open the **Run History** screen (scroll with the mouse wheel, ESC to close).
The best run is highlighted in gold with a rainbow border.

History is stored in `config/obtain-speedrun-history.json`.

## Requirements

- Minecraft Java Edition **1.21.11**
- Fabric Loader 0.18.4+
- Fabric API (matching 1.21.11)
- Java 21

## Building the mod on your computer

1. Install JDK 21.
2. Unzip this folder and open a terminal in it.
3. Generate the Gradle wrapper once if it is missing: `gradle wrapper` (needs Gradle installed), or copy `gradlew`, `gradlew.bat` and `gradle/` from the Fabric example mod template at https://github.com/FabricMC/fabric-example-mod
4. Run `./gradlew build` (Windows: `gradlew.bat build`).
5. The jar is in `build/libs/obtain-speedrun-1.0.0.jar`. Put it in your `mods` folder with Fabric API.

## Putting it on GitHub

```
git init
git add .
git commit -m "Obtain speedrun mod"
git branch -M main
git remote add origin https://github.com/YOUR_NAME/obtain-speedrun.git
git push -u origin main
```

Add a `.gitignore` containing `build/`, `.gradle/`, `.idea/` and `run/` before the first commit.

## Tuning

- `ItemPool.java` - the `EXCLUDED` set controls which vanilla items can be picked.
- `RunManager.java` - `SPIN_MS`, `REVEAL_MS` and `DONE_MS` control the timing of each stage.
- `HudRenderer.java` - colours and layout of the spin, reveal, timer bar and splash.

## Notes

- Only singleplayer worlds start a run. Multiplayer servers are ignored.
- The pool only includes items in the `minecraft` namespace, so modded items are never chosen.
- Items already in your inventory when a run starts are excluded from the pick.
- If you leave a world mid-run, that run is abandoned and not saved.
- Versions in `gradle.properties` must match Fabric's current release for 1.21.11. Check https://fabricmc.net/develop/ if the build can't resolve them.
