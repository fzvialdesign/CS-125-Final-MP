package edu.illinois.cs.cs125.fall2020.mp.models;

/** Extends Summary for more detailed data. */
public class Course extends Summary {
  private String description;

  /**
   * See below for what method does.
   *
   * @return detailed course description
   */
  public String getDescription() {
    return description;
  }

  /** Create an empty Course. */
  public Course() {}

  /**
   * Creates Course based on variables below.
   *
   * @param setYear to year (super)
   * @param setSemester to semester (super)
   * @param setDepartment to course department (super)
   * @param setNumber to course number (super)
   * @param setTitle to course title (super)
   * @param setDescription to detailed course description
   */
  public Course(
      final String setYear,
      final String setSemester,
      final String setDepartment,
      final String setNumber,
      final String setTitle,
      final String setDescription) {
    super(setYear, setSemester, setDepartment, setNumber, setTitle);
    description = setDescription;
  }
}
