import java.util.List;

public class StudentService implements ManagementOperations<Student> {
    private final StudentDao studentDao;

    public StudentService() {
        this(new StudentDao());
    }

    public StudentService(StudentDao studentDao) {
        if (studentDao == null) {
            throw new IllegalArgumentException("Student database access is required.");
        }
        this.studentDao = studentDao;
    }

    @Override
    public void addRecord(Student student) {
        addStudent(student);
    }

    @Override
    public List<Student> getAllRecords() {
        return getAllStudents();
    }

    @Override
    public Student findRecord(String studentId) {
        return findStudent(studentId);
    }

    @Override
    public boolean updateRecord(String currentId, Student updatedStudent) {
        return updateStudent(currentId, updatedStudent);
    }

    @Override
    public boolean deleteRecord(String studentId) {
        return deleteStudent(studentId);
    }

    public void addStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student details are required.");
        }
        studentDao.add(student);
    }

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public Student findStudent(String studentId) {
        return studentDao.findById(studentId == null ? null : studentId.trim());
    }

    public boolean updateStudent(String currentId, Student updatedStudent) {
        if (updatedStudent == null) {
            throw new IllegalArgumentException("Updated student details are required.");
        }
        return studentDao.update(currentId, updatedStudent);
    }

    public boolean deleteStudent(String studentId) {
        return studentDao.delete(studentId);
    }
}
