import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyDao {
    public void add(Faculty faculty) {
        String sql = "INSERT INTO faculty (faculty_id, name, department, email, phone_number) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, faculty.getFacultyId());
            statement.setString(2, faculty.getFacultyName());
            statement.setString(3, faculty.getDepartment());
            statement.setString(4, faculty.getEmail());
            statement.setString(5, faculty.getPhoneNumber());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("adding the faculty member", exception);
        }
    }

    public List<Faculty> findAll() {
        String sql = "SELECT faculty_id, name, department, email, phone_number "
                + "FROM faculty ORDER BY faculty_id";
        ArrayList<Faculty> facultyMembers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                facultyMembers.add(new Faculty(results.getString("faculty_id"), results.getString("name"),
                        results.getString("department"), results.getString("email"),
                        results.getString("phone_number")));
            }
            return facultyMembers;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("listing faculty", exception);
        }
    }

    public Faculty findById(String facultyId) {
        String sql = "SELECT faculty_id, name, department, email, phone_number "
                + "FROM faculty WHERE faculty_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, facultyId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                return new Faculty(results.getString("faculty_id"), results.getString("name"),
                        results.getString("department"), results.getString("email"),
                        results.getString("phone_number"));
            }
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("searching for the faculty member", exception);
        }
    }

    public boolean update(String currentId, Faculty faculty) {
        String sql = "UPDATE faculty SET faculty_id = ?, name = ?, department = ?, email = ?, phone_number = ? "
                + "WHERE faculty_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, faculty.getFacultyId());
            statement.setString(2, faculty.getFacultyName());
            statement.setString(3, faculty.getDepartment());
            statement.setString(4, faculty.getEmail());
            statement.setString(5, faculty.getPhoneNumber());
            statement.setString(6, currentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("updating the faculty member", exception);
        }
    }

    public boolean delete(String facultyId) {
        String sql = "DELETE FROM faculty WHERE faculty_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, facultyId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw DatabaseException.forOperation("deleting the faculty member", exception);
        }
    }
}
