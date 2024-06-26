package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.adapters.CourseListAdapter;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityMainBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;
import java.util.Arrays;
import java.util.List;

/** Main activity showing the course summary list. */
public final class MainActivity extends AppCompatActivity
    implements SearchView.OnQueryTextListener,
        SortedListAdapter.Callback,
        Client.CourseClientCallbacks,
        CourseListAdapter.Listener {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = MainActivity.class.getSimpleName();

  // You should not need to modify these values.
  // At this point you only have data for this semester anyway.
  private static final String DEFAULT_YEAR = "2020";
  private static final String DEFAULT_SEMESTER = "fall";

  // Binding to the layout in activity_main.xml
  private ActivityMainBinding binding;
  // Adapter that connects our list of courses with the list displayed on the display
  private CourseListAdapter listAdapter;
  // List of courses retrieved from the backend server
  @SuppressWarnings("FieldCanBeLocal")
  private List<Summary> courses;

  /**
   * Called when this activity is created.
   *
   * <p>Because this is the main activity for this app, this method is called when the app is
   * started, and any time that this view is shown.
   *
   * @param unused saved instance state, currently unused and always empty or null
   */
  @Override
  protected void onCreate(final Bundle unused) {
    super.onCreate(unused);
    Log.i("Startup", "onCreate in Application");

    // Bind to the layout in activity_main.xml
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    // Setup the list adapter for the list of courses
    listAdapter = new CourseListAdapter(this, this);
    listAdapter.addCallback(this);
    binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    binding.recyclerView.setAdapter(listAdapter);

    // Retrieve the API client from the application and initiate a course summary request
    CourseableApplication application = (CourseableApplication) getApplication();
    application.getCourseClient().getSummary(DEFAULT_YEAR, DEFAULT_SEMESTER, this);

    // Register this component as a callback for changes to the search view component shown above
    // the course list
    // We use these events to initiate course list filtering
    binding.search.setOnQueryTextListener(this);

    // Register our toolbar
    setSupportActionBar(binding.toolbar);
  }

  /**
   * Callback called when the client has retrieved the list of courses for this component to
   * display.
   *
   * @param year the year that was retrieved
   * @param semester the semester that was retrieved
   * @param setSummaries the summaries returned from the course API client
   */
  @Override
  public void summaryResponse(
      final String year, final String semester, final Summary[] setSummaries) {
    courses = Arrays.asList(setSummaries);
    listAdapter.edit().replaceAll(courses).commit();
  }

  /**
   * Callback fired when the course list view component begins editing the list of visible courses.
   */
  @Override
  public void onEditStarted() {}

  /**
   * Callback fired when the course list view component completes editing the list of visible
   * courses.
   */
  @Override
  public void onEditFinished() {
    binding.recyclerView.scrollToPosition(0);
  }

  /**
   * Callback fired when the user submits a search query.
   *
   * <p>Because we update the list on each change to the search value, we do not handle this
   * callback.
   *
   * @param unused current query text
   * @return true because we handled this action
   */
  @Override
  public boolean onQueryTextSubmit(final String unused) {
    return true;
  }

  /**
   * Callback fired when the user edits the text in the search query box.
   *
   * <p>We handle this by updating the list of visible courses.
   *
   * @param query the text to use to filter the course list
   * @return true because we handled the action
   */
  @Override
  public boolean onQueryTextChange(final String query) {
    listAdapter.edit().replaceAll(Summary.filter(courses, query)).commit();
    return true;
  }

  /**
   * Callback fired when a user clicks on a course in the list view.
   *
   * <p>You will handle this as part of MP1.
   *
   * @param summary the course that was clicked
   */
  @Override
  public void onCourseClicked(final Summary summary) {
    try {
      Log.d(TAG, "Clicked on " + summary.getTitle());

      ObjectMapper mapper = new ObjectMapper();
      Intent startCourseActivity = new Intent(this, CourseActivity.class);

      startCourseActivity.putExtra("COURSE", mapper.writeValueAsString(summary));
      startActivity(startCourseActivity);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
