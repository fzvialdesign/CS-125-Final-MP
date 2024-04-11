package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Course activity showing the detailed course description. */
public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {
  private static final String TAG = CourseActivity.class.getSimpleName();

  /**
   * Deserializes information to send off the description to its page.
   *
   * @param savedInstanceState retrieves a saved instance state
   */
  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    ObjectMapper mapper = new ObjectMapper();
    CourseableApplication application = (CourseableApplication) getApplication();
    ActivityCourseBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    try {
      Client client = application.getCourseClient();
      Summary summary = mapper.readValue(intent.getStringExtra("COURSE"), Summary.class);
      CompletableFuture<Course> completableFutureC = new CompletableFuture<>();
      CompletableFuture<Rating> completableFutureR = new CompletableFuture<>();

      client.getCourse(
          summary,
          new Client.CourseClientCallbacks() {
            @Override
            public void courseResponse(final Summary summary, final Course course) {
              completableFutureC.complete(course);
            }
          });
      Course course = completableFutureC.get();
      String full = summary.getDepartment() + " " + summary.getNumber() + ": " + summary.getTitle();
      binding.title.setText(full);
      binding.desc.setText(course.getDescription());

      String clientID = application.getClientID();
      client.getRating(
          course,
          clientID,
          new Client.CourseClientCallbacks() {
            @Override
            public void yourRating(final Summary summary, final Rating rating) {
              completableFutureR.complete(rating);
            }
          });
      Rating rate = completableFutureR.get();
      binding.rating.setRating((float) rate.getRating());
      System.out.println(rate.getRating());

      binding.rating.setOnRatingBarChangeListener(
          (ratingBar, rate1, fromUser) -> {
            Rating rating = new Rating(clientID, rate1);
            client.postRating(
                summary,
                rating,
                new Client.CourseClientCallbacks() {
                  @Override
                  public void yourRating(final Summary summary1, final Rating rating) {
                    completableFutureR.complete(rating);
                  }
                });
          });
    } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
