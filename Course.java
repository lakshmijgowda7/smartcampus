public class Course {
    private String courseId;
    private String courseName;
    private String courseCode;
    private int credits;
    private String department;

    public Course(String courseId, String courseName, String courseCode, int credits, String department) {
        setCourseId(courseId);
        setCourseName(courseName);
        setCourseCode(courseCode);
        setCredits(credits);
        setDepartment(department);
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = requireText(courseId, "Course ID");
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = requireText(courseName, "Course name");
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = requireText(courseCode, "Course code");
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be a positive whole number.");
        }
        this.credits = credits;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = requireText(department, "Department");
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public void displayDetails() {
        System.out.println(this);
    }

    public void displayCourse() {
        displayDetails();
    }

    @Override
    public String toString() {
        return "Course ID: " + courseId
                + " | Name: " + courseName
                + " | Code: " + courseCode
                + " | Credits: " + credits
                + " | Department: " + department;
    }
}
