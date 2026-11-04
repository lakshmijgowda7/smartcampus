import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {
    public void add(Student student) {
        String sql = "INSERT INTO students (student_id, name, branch, section, semester) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getStudentId());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getBranch());
            statement.setString(4, student.getSection());
            statement.setInt(5, student.getSemester());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("adding the student", exception);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT student_id, name, branch, section, semester FROM students ORDER BY student_id";
        ArrayList<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                students.add(new Student(results.getString("student_id"), results.getString("name"),
                        results.getString("branch"), results.getString("section"),
                        results.getInt("semester")));
            }
            return students;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("listing students", exception);
        }
    }

    public Student findById(String studentId) {
        String sql = "SELECT student_id, name, branch, section, semester FROM students WHERE student_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new Student(results.getString("student_id"), results.getString("name"),
                        results.getString("branch"), results.getString("section"),
                        results.getInt("semester"));
            }
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("searching for the student", exception);
        }
    }

    public boolean update(String currentId, Student student) {
        String sql = "UPDATE students SET student_id = ?, name = ?, branch = ?, section = ?, semester = ? "
                + "WHERE student_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getStudentId());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getBranch());
            statement.setString(4, student.getSection());
            statement.setInt(5, student.getSemester());
            statement.setString(6, currentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("updating the student", exception);
        }
    }

    public boolean delete(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("deleting the student", exception);
        }
    }
}
