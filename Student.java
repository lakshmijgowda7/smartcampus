
public class Student extends Person {
    private String studentId;
    private String email;
    private String branch;
    private String section;
    private int semester;

    public Student(String studentId, String studentName, String branch, String section, int semester) {
        this(studentId, studentName, "", branch, section, semester);
    }

    public Student(String studentId, String studentName, String email,
                   String branch, String section, int semester) {
        super(studentName);
        setStudentId(studentId);
        setEmail(email);
        setBranch(branch);
        setSection(section);
        setSemester(semester);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = requireText(studentId, "Student USN");
    }

    public String getStudentName() {
        return getName();
    }

    public void setStudentName(String studentName) {
        setName(studentName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email.trim();
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = requireText(branch, "Department");
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        if (section == null || section.trim().isEmpty()) {
            this.section = null;
            return;
        }

        String normalized = section.trim().toUpperCase();
        if (!normalized.equals("A") && !normalized.equals("B") && !normalized.equals("C")) {
            throw new IllegalArgumentException("Section must be A, B, or C.");
        }
        this.section = normalized;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester < 1) {
            throw new IllegalArgumentException("Semester must be a positive number.");
        }
        this.semester = semester;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    @Override
    public void displayDetails() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Student USN: " + studentId
                + " | Name: " + getName()
                + " | Email: " + (email.isEmpty() ? "Not set" : email)
                + " | Department: " + branch
                + " | Section: " + (section == null ? "Not set" : section)
                + " | Semester: " + semester;
    }
}
