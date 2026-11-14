package org.project.r0kshan;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.log4j.Log4j2;

/**
 * Main class for JMonkey gamebase showcasing a minimal world with basic environnemental
 * interactions. Only bootstraps: each feature lives in its own app state.
 */
@Log4j2
public class Gamebase extends SimpleApplication {

  private static final String ASSETS_PATH = "src/main/resources/assets";

  /**
   * Constructor for the main class.
   */
  public Gamebase() {
    // No FlyCamAppState: the player's chase camera drives the view
    super(new StatsAppState(), new AudioListenerState(), new DebugKeysAppState());
  }

  /**
   * Main.
   *
   * @param args main arguments
   */
  public static void main(String[] args) {
    final Gamebase app = new Gamebase();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    final Path classesDir;
    try {
      classesDir = Paths.get(
          Gamebase.class.getProtectionDomain().getCodeSource().getLocation().toURI()
      );
    } catch (URISyntaxException e) {
      log.fatal("Failed to resolve asset path: {}", e.getMessage());
      // Tells jME to shut down the display and exit the main loop cleanly
      this.stop();
      return;
    }

    // target/classes (or the jar) sits two levels below the module root
    final Path moduleRoot = classesDir.resolve("../..").normalize();

    final String assetsPath = moduleRoot.resolve(ASSETS_PATH).toString();
    assetManager.registerLocator(assetsPath, FileLocator.class);
    assetManager.registerLocator(assetsPath, ClasspathLocator.class);

    stateManager.attach(new BulletAppState());
    stateManager.attach(new WorldAppState());
    stateManager.attach(new PlayerAppState());
  }
}
