import java.util.List;

public class FacultyService implements ManagementOperations<Faculty> {
    private final FacultyDao facultyDao;

    public FacultyService() {
        this(new FacultyDao());
    }

    public FacultyService(FacultyDao facultyDao) {
        if (facultyDao == null) {
            throw new IllegalArgumentException("Faculty database access is required.");
        }
        this.facultyDao = facultyDao;
    }

    @Override
    public void addRecord(Faculty faculty) {
        addFaculty(faculty);
    }

    @Override
    public List<Faculty> getAllRecords() {
        return getAllFaculty();
    }

    @Override
    public Faculty findRecord(String facultyId) {
        return findFaculty(facultyId);
    }

    @Override
    public boolean updateRecord(String currentId, Faculty updatedFaculty) {
        return updateFaculty(currentId, updatedFaculty);
    }

    @Override
    public boolean deleteRecord(String facultyId) {
        return deleteFaculty(facultyId);
    }

    public void addFaculty(Faculty faculty) {
        if (faculty == null) {
            throw new IllegalArgumentException("Faculty details are required.");
        }
        facultyDao.add(faculty);
    }

    public List<Faculty> getAllFaculty() {
        return facultyDao.findAll();
    }

    public Faculty findFaculty(String facultyId) {
        return facultyDao.findById(facultyId == null ? null : facultyId.trim());
    }

    public boolean updateFaculty(String currentId, Faculty updatedFaculty) {
        if (updatedFaculty == null) {
            throw new IllegalArgumentException("Updated faculty details are required.");
        }
        return facultyDao.update(currentId, updatedFaculty);
    }

    public boolean deleteFaculty(String facultyId) {
        return facultyDao.delete(facultyId);
    }
}
