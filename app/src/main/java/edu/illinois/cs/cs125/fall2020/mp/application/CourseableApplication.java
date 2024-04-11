package edu.illinois.cs.cs125.fall2020.mp.application;

import android.app.Application;
import android.util.Log;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;
import edu.illinois.cs.cs125.fall2020.mp.network.Server;
import java.util.UUID;

/**
 * Application class for the Courseable app.
 *
 * <p>Starts the development server and creates the course API client.
 *
 * <p>You should not need to modify this file.
 */
public class CourseableApplication extends Application {
  /** Course API server port. */
  public static final int SERVER_PORT = 8888;
  /** Course API server URL. */
  public static final String SERVER_URL = "http://localhost:" + SERVER_PORT + "/";

  // Course API client created during application startup
  private Client client;

  private String clientID = UUID.randomUUID().toString();

  @Override
  public final void onCreate() {
    Log.i("Startup", "onCreate in Application");
    super.onCreate();
    client = Client.start();
    Server.start();
  }

  /**
   * Retrieve the course API client instance for this app.
   *
   * @return the course API client instance.
   */
  public final Client getCourseClient() {
    return client;
  }

  /**
   * Retrieve the course API client UUID for this app.
   *
   * @return the course API client UUID.
   */
  public final String getClientID() {
    return clientID;
  }
}
