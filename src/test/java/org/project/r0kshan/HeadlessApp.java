package org.project.r0kshan;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Runs a real jME update loop without a window, so app states can be tested in CI.
 */
final class HeadlessApp extends SimpleApplication {

  private static final long TIMEOUT_SECONDS = 10;
  private final CountDownLatch ready = new CountDownLatch(1);

  private HeadlessApp() {
    super((AppState[]) null);
  }

  /**
   * Starts a headless app and attaches the states once natives (Bullet) are loaded, which
   * only happens on start. Returns after the states are initialized.
   */
  @SafeVarargs
  static HeadlessApp start(final Supplier<AppState>... states) throws Exception {
    final HeadlessApp app = new HeadlessApp();
    final AppSettings settings = new AppSettings(true);
    settings.setAudioRenderer(null);
    settings.setFrameRate(120);
    app.setSettings(settings);
    app.setShowSettings(false);
    app.start(JmeContext.Type.Headless);
    if (!app.ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
      throw new IllegalStateException("Headless app did not start");
    }
    app.run(() -> {
      for (final Supplier<AppState> state : states) {
        app.getStateManager().attach(state.get());
      }
    });
    // Attached states initialize during the following frame
    app.run(() -> { });
    return app;
  }

  @Override
  public void simpleInitApp() {
    assetManager.registerLocator("src/main/resources/assets", FileLocator.class);
    ready.countDown();
  }

  /**
   * Runs the task on the render thread and returns its result.
   */
  <T> T call(final Callable<T> task) throws Exception {
    return enqueue(task).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * Runs the action on the render thread and waits for it. jME's AppTask reports a null
   * result as a timeout, so a non-null marker is returned.
   */
  void run(final Runnable action) throws Exception {
    call(() -> {
      action.run();
      return Boolean.TRUE;
    });
  }
}
