import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    public void add(Course course) {
        String sql = "INSERT INTO courses (course_id, course_name, course_code, credits, department) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getCourseId());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getCourseCode());
            statement.setInt(4, course.getCredits());
            statement.setString(5, course.getDepartment());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("adding the course", exception);
        }
    }

    public List<Course> findAll() {
        String sql = "SELECT course_id, course_name, course_code, credits, department "
                + "FROM courses ORDER BY course_id";
        ArrayList<Course> courses = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                courses.add(new Course(results.getString("course_id"), results.getString("course_name"),
                        results.getString("course_code"), results.getInt("credits"),
                        results.getString("department")));
            }
            return courses;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("listing courses", exception);
        }
    }

    public Course findById(String courseId) {
        String sql = "SELECT course_id, course_name, course_code, credits, department "
                + "FROM courses WHERE course_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new Course(results.getString("course_id"), results.getString("course_name"),
                        results.getString("course_code"), results.getInt("credits"),
                        results.getString("department"));
            }
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("searching for the course", exception);
        }
    }

    public boolean update(String currentId, Course course) {
        String sql = "UPDATE courses SET course_id = ?, course_name = ?, course_code = ?, credits = ?, "
                + "department = ? WHERE course_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getCourseId());
            statement.setString(2, course.getCourseName());
            statement.setString(3, course.getCourseCode());
            statement.setInt(4, course.getCredits());
            statement.setString(5, course.getDepartment());
            statement.setString(6, currentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("updating the course", exception);
        }
    }

    public boolean delete(String courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("deleting the course", exception);
        }
    }
}
