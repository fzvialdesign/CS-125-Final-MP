package edu.illinois.cs.cs125.fall2020.mp.network;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();

  // summary/YEAR/SEMESTER
  private MockResponse getSummary(@NonNull final String path) {
    String[] parts = path.split("/");
    final int curLength = 2;
    if (parts.length != curLength) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = summaries.get(parts[0] + "_" + parts[1]);
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>();

  // course/YEAR/SEMESTER/DEPARTMENT/NUMBER
  private MockResponse getCourse(@NonNull final String path) {
    String[] parts = path.split("/");
    final int curLength = 4;
    if (parts.length != curLength) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String course = courses.get(new Summary(parts[0], parts[1], parts[2], parts[3], ""));
    if (course == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(course);
  }

  private final Map<Summary, Map<String, Rating>> ratings = new HashMap<>();

  // rating/YEAR/SEMESTER/DEPARTMENT/NUMBER?client=UUID
  private MockResponse getOrPostRating(@NonNull final RecordedRequest request) {
    String path = request.getPath().replaceFirst("/rating/", "");
    String[] parts = path.split("/|\\?client=");
    Summary summary = new Summary(parts[0], parts[1], parts[2], parts[3], "");
    final int prtLength = 5, uuidLength = 36, mgcNum = 4;
    if (parts.length == prtLength && parts[mgcNum].length() == uuidLength) {
      Set<Summary> set = ratings.keySet();
      if (set.contains(summary)) {
        if (request.getMethod().equals("GET")) {
          if (ratings.get(summary) == null) {
            ratings.put(summary, new HashMap<>());
          }
          if (ratings.get(summary).get(parts[mgcNum]) == null) {
            ratings.get(summary).put(parts[mgcNum], new Rating(parts[mgcNum], Rating.NOT_RATED));
          }
          try {
            return new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mapper.writeValueAsString(ratings.get(summary).get(parts[mgcNum])));
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        } else if (request.getMethod().equals("POST")) {
          try {
            Rating rating = mapper.readValue(request.getBody().readUtf8(), Rating.class);
            ratings.get(summary).put(parts[mgcNum], rating);
            System.out.println(request.getPath());
            return new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .setHeader("Location", request.getPath());
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        }
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }

  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {
    try {
      String path = request.getPath();
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("HEAD")) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", ""));
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/")) {
        return getOrPostRating(request);
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  private static boolean started = false;

  /**
   * Start the server if has not already been started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!started) {
      new Thread(Server::new).start();
      started = true;
    }
  }

  private final ObjectMapper mapper = new ObjectMapper();

  private Server() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2020", "fall");
    loadCourses("2020", "fall");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.SERVER_PORT);

      String baseUrl = server.url("").toString();
      if (!CourseableApplication.SERVER_URL.equals(baseUrl)) {
        throw new IllegalStateException("Bad server URL: " + baseUrl);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString());
        ratings.put(course, new HashMap<>());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
