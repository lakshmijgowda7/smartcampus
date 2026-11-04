import java.util.List;

public class CourseService implements ManagementOperations<Course> {
    private final CourseDao courseDao;

    public CourseService() {
        this(new CourseDao());
    }

    public CourseService(CourseDao courseDao) {
        if (courseDao == null) {
            throw new IllegalArgumentException("Course database access is required.");
        }
        this.courseDao = courseDao;
    }

    @Override
    public void addRecord(Course course) {
        addCourse(course);
    }

    @Override
    public List<Course> getAllRecords() {
        return getAllCourses();
    }

    @Override
    public Course findRecord(String courseId) {
        return findCourse(courseId);
    }

    @Override
    public boolean updateRecord(String currentId, Course updatedCourse) {
        return updateCourse(currentId, updatedCourse);
    }

    @Override
    public boolean deleteRecord(String courseId) {
        return deleteCourse(courseId);
    }

    public void addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course details are required.");
        }
        courseDao.add(course);
    }

    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    public Course findCourse(String courseId) {
        return courseDao.findById(courseId == null ? null : courseId.trim());
    }

    public boolean updateCourse(String currentId, Course updatedCourse) {
        if (updatedCourse == null) {
            throw new IllegalArgumentException("Updated course details are required.");
        }
        return courseDao.update(currentId, updatedCourse);
    }

    public boolean deleteCourse(String courseId) {
        return courseDao.delete(courseId);
    }
}
