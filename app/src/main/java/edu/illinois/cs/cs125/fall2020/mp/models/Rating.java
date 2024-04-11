package edu.illinois.cs.cs125.fall2020.mp.models;

/** Rating class for storing client ratings of courses. */
public class Rating {
  /** Rating indicated that the course has not been rated yet. */
  public static final double NOT_RATED = -1.0;

  private String id;
  private double rating;

  /** Creates empty rating. */
  public Rating() {}

  /**
   * Creates rating.
   *
   * @param setId to ID to use
   * @param setRating to rating to use
   */
  public Rating(final String setId, final double setRating) {
    id = setId;
    rating = setRating;
  }

  /**
   * Retrieves ID of the Rating.
   *
   * @return id
   */
  public final String getId() {
    return id;
  }

  /**
   * Retrieves rating of the Rating.
   *
   * @return rating
   */
  public final double getRating() {
    return rating;
  }
}
