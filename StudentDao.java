
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    public void add(Student student) {
        String sql = "INSERT INTO students "
                + "(usn, full_name, email, department, section, semester) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getStudentId());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getEmail());
            statement.setString(4, student.getBranch());
            statement.setString(5, student.getSection());
            statement.setInt(6, student.getSemester());

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw DatabaseException.forOperation("adding the student", exception);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT usn, full_name, email, department, section, semester "
                + "FROM students ORDER BY usn";

        List<Student> students = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                students.add(mapStudent(results));
            }

            return students;

        } catch (SQLException exception) {
            throw DatabaseException.forOperation("listing students", exception);
        }
    }

    public Student findById(String studentId) {
        String sql = "SELECT usn, full_name, email, department, section, semester "
                + "FROM students WHERE usn = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentId);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return mapStudent(results);
                }
                return null;
            }

        } catch (SQLException exception) {
            throw DatabaseException.forOperation("searching for the student", exception);
        }
    }

    public boolean update(String currentId, Student student) {
        String sql = "UPDATE students SET usn = ?, full_name = ?, email = ?, "
                + "department = ?, section = ?, semester = ? WHERE usn = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getStudentId());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getEmail());
            statement.setString(4, student.getBranch());
            statement.setString(5, student.getSection());
            statement.setInt(6, student.getSemester());
            statement.setString(7, currentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw DatabaseException.forOperation("updating the student", exception);
        }
    }

    public boolean delete(String studentId) {
        String sql = "DELETE FROM students WHERE usn = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentId);
            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw DatabaseException.forOperation("deleting the student", exception);
        }
    }

    private Student mapStudent(ResultSet results) throws SQLException {
        return new Student(
                results.getString("usn"),
                results.getString("full_name"),
                results.getString("email"),
                results.getString("department"),
                results.getString("section"),
                results.getInt("semester")
        );
    }
}
